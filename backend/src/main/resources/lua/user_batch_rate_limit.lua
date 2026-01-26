-- KEYS[1] = key for batch limit
-- ARGV[1] = TTL (time to wait in seconds)

local current = redis.call('INCR', KEYS[1])
if current == 1 then
    redis.call('EXPIRE', KEYS[1], ARGV[1])
end
return current