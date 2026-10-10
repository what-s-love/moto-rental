#!/usr/bin/env bash
set -euo pipefail

DB_NAME="moto_trip_db"
BACKUP_DIR="/srv/moto-trip/backups"
KEEP_DAYS=14
S3_BUCKET="s3://mototrip-db-backups"
S3_ENDPOINT="https://s3.regru.cloud"
TIMESTAMP=$(date +%F)
DUMP_FILE="${BACKUP_DIR}/db_${TIMESTAMP}.dump"
LOG_FILE="${BACKUP_DIR}/backup.log"

echo "[$(date '+%F %T')] Начало бэкапа ${DB_NAME}" >> "${LOG_FILE}"

# Дамп в custom-формате (-Fc) — сжатый, быстро восстанавливается через pg_restore
pg_dump -Fc "${DB_NAME}" > "${DUMP_FILE}"

echo "[$(date '+%F %T')] Дамп записан: ${DUMP_FILE} ($(du -sh "${DUMP_FILE}" | cut -f1))" >> "${LOG_FILE}"

# Загрузка в S3
aws s3 cp "${DUMP_FILE}" "${S3_BUCKET}/db-backups/$(basename ${DUMP_FILE})" \
    --endpoint-url "${S3_ENDPOINT}" \
    2>> "${LOG_FILE}" \
    && echo "[$(date '+%F %T')] Загружено в S3" >> "${LOG_FILE}" \
    || echo "[$(date '+%F %T')] ОШИБКА загрузки в S3" >> "${LOG_FILE}"

# Ротация: удалить дампы старше KEEP_DAYS дней
find "${BACKUP_DIR}" -name "db_*.dump" -mtime "+${KEEP_DAYS}" -delete

echo "[$(date '+%F %T')] Ротация выполнена (хранить ${KEEP_DAYS} дней)" >> "${LOG_FILE}"
echo "---" >> "${LOG_FILE}"