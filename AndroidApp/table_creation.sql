-- WARNING: This will delete ALL data in these tables!
DROP TABLE IF EXISTS immutable_categories CASCADE;
DROP TABLE IF EXISTS mutable_categories CASCADE;
DROP TABLE IF EXISTS invitations CASCADE;
DROP TABLE IF EXISTS announcements CASCADE;
DROP TABLE IF EXISTS attendees CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TYPE IF EXISTS attendee_role CASCADE;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
DROP TYPE IF EXISTS attendee_role CASCADE;
CREATE TYPE attendee_role AS ENUM ('HOST', 'CO_HOST', 'GUEST', 'INVITEE');

DROP TYPE IF EXISTS invitation_status CASCADE;
CREATE TYPE invitation_status AS ENUM ('PENDING', 'ACCEPTED', 'REJECTED');


CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    avatar_url TEXT NOT NULL,
    bio TEXT DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT email_format CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$')
);

CREATE TABLE IF NOT EXISTS events (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    host_id TEXT NOT NULL,
    date_time TIMESTAMPTZ NOT NULL,
    location TEXT NOT NULL,
    description TEXT DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_host FOREIGN KEY (host_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT title_not_blank CHECK (title <> ''),
    CONSTRAINT location_not_blank CHECK (location <> '')
);

CREATE TABLE IF NOT EXISTS attendees (
    id TEXT PRIMARY KEY DEFAULT uuid_generate_v4()::TEXT,
    user_id TEXT NOT NULL,
    event_id TEXT NOT NULL,
    name TEXT NOT NULL,
    role attendee_role NOT NULL DEFAULT 'INVITEE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_event UNIQUE(user_id, event_id)
);

CREATE TABLE IF NOT EXISTS announcements (
    id TEXT PRIMARY KEY DEFAULT uuid_generate_v4()::TEXT,
    event_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    text TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT text_not_blank CHECK (text <> '')
);

CREATE TABLE IF NOT EXISTS invitations (
    id TEXT PRIMARY KEY DEFAULT uuid_generate_v4()::TEXT,
    event_id TEXT NOT NULL,
    inviter_id TEXT NOT NULL,
    invitee_id TEXT NOT NULL,
    status invitation_status NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_inviter FOREIGN KEY (inviter_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_invitee FOREIGN KEY (invitee_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT different_users CHECK (inviter_id <> invitee_id),
    CONSTRAINT unique_invitation UNIQUE(event_id, invitee_id)
);


CREATE TABLE IF NOT EXISTS mutable_categories (
    id TEXT PRIMARY KEY DEFAULT uuid_generate_v4()::TEXT,
    event_id TEXT NOT NULL,
    name TEXT NOT NULL,
    items JSONB DEFAULT '{}'::JSONB,

    CONSTRAINT fk_mutable_category_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT mutable_category_name_not_blank CHECK (name <> ''),
    CONSTRAINT unique_mutable_category_per_event UNIQUE(event_id, name)
);

CREATE TABLE IF NOT EXISTS immutable_categories (
    id TEXT PRIMARY KEY DEFAULT uuid_generate_v4()::TEXT,
    event_id TEXT NOT NULL,
    name TEXT NOT NULL,
    items JSONB DEFAULT '{}'::JSONB,

    CONSTRAINT fk_immutable_category_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT immutable_category_name_not_blank CHECK (name <> ''),
    CONSTRAINT unique_immutable_category_per_event UNIQUE(event_id, name)
);

-- ============================================================================
-- INDEXES for better query performance
-- ============================================================================

-- Users table indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Events table indexes
CREATE INDEX IF NOT EXISTS idx_events_host_id ON events(host_id);
CREATE INDEX IF NOT EXISTS idx_events_date_time ON events(date_time);

-- Attendees table indexes
CREATE INDEX IF NOT EXISTS idx_attendees_user_id ON attendees(user_id);
CREATE INDEX IF NOT EXISTS idx_attendees_event_id ON attendees(event_id);
CREATE INDEX IF NOT EXISTS idx_attendees_role ON attendees(role);

-- Announcements table indexes
CREATE INDEX IF NOT EXISTS idx_announcements_event_id ON announcements(event_id);
CREATE INDEX IF NOT EXISTS idx_announcements_user_id ON announcements(user_id);
CREATE INDEX IF NOT EXISTS idx_announcements_created_at ON announcements(created_at DESC);

-- Invitations table indexes
CREATE INDEX IF NOT EXISTS idx_invitations_event_id ON invitations(event_id);
CREATE INDEX IF NOT EXISTS idx_invitations_inviter_id ON invitations(inviter_id);
CREATE INDEX IF NOT EXISTS idx_invitations_invitee_id ON invitations(invitee_id);
CREATE INDEX IF NOT EXISTS idx_invitations_status ON invitations(status);
CREATE INDEX IF NOT EXISTS idx_invitations_sent_at ON invitations(sent_at DESC);

-- Mutable categories table indexes
CREATE INDEX IF NOT EXISTS idx_mutable_categories_event_id ON mutable_categories(event_id);

-- Immutable categories table indexes
CREATE INDEX IF NOT EXISTS idx_immutable_categories_event_id ON immutable_categories(event_id);

-- ============================================================================
-- TRIGGERS for automatic updated_at timestamp management
-- ============================================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger to all tables with updated_at column
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_events_updated_at ON events;
CREATE TRIGGER update_events_updated_at
    BEFORE UPDATE ON events
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_attendees_updated_at ON attendees;
CREATE TRIGGER update_attendees_updated_at
    BEFORE UPDATE ON attendees
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_announcements_updated_at ON announcements;
CREATE TRIGGER update_announcements_updated_at
    BEFORE UPDATE ON announcements
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
