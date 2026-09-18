ALTER TABLE outbox_events
    ADD COLUMN correlation_id VARCHAR(128);

UPDATE outbox_events
SET correlation_id = 'legacy-' || id::text
WHERE correlation_id IS NULL;

ALTER TABLE outbox_events
ALTER COLUMN correlation_id SET NOT NULL;