CREATE TABLE document_workflow (
                                   document_id UUID PRIMARY KEY,
                                   doc_ref UUID NOT NULL,
                                   request_uid UUID NOT NULL UNIQUE,
                                   current_step VARCHAR(64) NOT NULL,
                                   document_name VARCHAR(255),
                                   content_size_bytes BIGINT,
                                   failed_at_step VARCHAR(64),
                                   failure_reason VARCHAR(2000),
                                   retry_count INTEGER NOT NULL,
                                   created_at TIMESTAMP NOT NULL,
                                   updated_at TIMESTAMP NOT NULL
);

CREATE TABLE step_result (
                             id UUID PRIMARY KEY,
                             document_id UUID NOT NULL,
                             step VARCHAR(64) NOT NULL,
                             status VARCHAR(32) NOT NULL,
                             started_at TIMESTAMP,
                             completed_at TIMESTAMP,
                             duration_ms BIGINT,
                             result_json TEXT,
                             error_message TEXT,
                             CONSTRAINT uk_step_result_document_step UNIQUE (document_id, step)
);

CREATE INDEX idx_document_workflow_current_step
    ON document_workflow(current_step);

CREATE INDEX idx_step_result_document_id
    ON step_result(document_id);

ALTER TABLE step_result
    ADD CONSTRAINT fk_step_result_document
        FOREIGN KEY (document_id) REFERENCES document_workflow(document_id);