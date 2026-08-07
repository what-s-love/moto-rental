UPDATE settings
SET name          = 'Период ожидания оплаты (минуты)',
    description   = 'Количество минут, отведённых клиенту на внесение предоплаты после создания бронирования до момента удаления бронирования',
    setting_value = '30',
    updated_at    = '2026-08-07 13:37:00'
WHERE setting_key = 'PREPAYMENT_PERIOD';