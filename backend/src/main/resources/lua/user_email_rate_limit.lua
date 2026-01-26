local now = tonumber(ARGV[1])
local hourWindow = tonumber(ARGV[2])
local dayWindow = tonumber(ARGV[3])
local hourLimit = tonumber(ARGV[4])
local dayLimit = tonumber(ARGV[5])
local count = tonumber(ARGV[6])

redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, now - hourWindow)
redis.call('ZREMRANGEBYSCORE', KEYS[2], 0, now - dayWindow)

local hourCount = redis.call('ZCARD', KEYS[1])
local dayCount = redis.call('ZCARD', KEYS[2])

if (hourCount + count > hourLimit) or (dayCount + count > dayLimit) then
    return 0
end

for i = 1, count do
    local member = now .. ":" .. i
    redis.call('ZADD', KEYS[1], now, member)
    redis.call('ZADD', KEYS[2], now, member)
end

redis.call('EXPIRE', KEYS[1], math.floor(hourWindow / 1000) + 60)
redis.call('EXPIRE', KEYS[2], math.floor(dayWindow / 1000) + 60)

return 1