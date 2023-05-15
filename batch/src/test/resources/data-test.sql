-- Servizio di test

-- Le tabelle del batch vengono create DOPO l'esecuzione di questo script.
-- Per fare in modo che i test si ritrovino una situazione pulita, droppo le tabelle ora in modo
-- che poi vengano ricreate nuove.

DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_PARAMS;
DROP TABLE IF EXISTS  BATCH_JOB_EXECUTION_CONTEXT;

DROP TABLE IF EXISTS BATCH_STEP_EXECUTION_CONTEXT;
DROP TABLE IF EXISTS BATCH_STEP_EXECUTION;

DROP TABLE IF EXISTS BATCH_JOB_EXECUTION;

DROP TABLE IF EXISTS BATCH_JOB_INSTANCE;
