-- KEYS[1..3] minute, hour, day keys
-- ARGV[1] = now (timestamp), ARGV[2] = count, ARGV[3..5] windows, ARGV[6..8] limits

local now = tonumber(ARGV[1])
local count = tonumber(ARGV[2])

local windows = {
    {key = KEYS[1], window = tonumber(ARGV[3]), limit = tonumber(ARGV[6])},
    {key = KEYS[2], window = tonumber(ARGV[4]), limit = tonumber(ARGV[7])},
    {key = KEYS[3], window = tonumber(ARGV[5]), limit = tonumber(ARGV[8])}
}

for i, w in ipairs(windows) do
    redis.call('ZREMRANGEBYSCORE', w.key, 0, now - w.window)
    local current = redis.call('ZCARD', w.key)
    if current + count > w.limit then
        return 0
    end
end

for i, w in ipairs(windows) do
    for j = 1, count do
        local member = now .. ":" .. j
        redis.call('ZADD', w.key, now, member)
    end
    redis.call('EXPIRE', w.key, math.floor(w.window / 1000) + 10)
end

return 1