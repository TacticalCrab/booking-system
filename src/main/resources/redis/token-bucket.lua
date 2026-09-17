local key = KEYS[1]

local capacity = tonumber(ARGV[1])
local refillPerMinute = tonumber(ARGV[2])
local ttlSeconds = tonumber(ARGV[3])

local redisTime = redis.call("TIME")

local nowMills =
    tonumber(redisTime[1]) * 1000 +
    math.floor(tonumber(redisTime[2]) / 1000)

local bucket = redis.call(
    "HMGET",
    key,
    "tokens",
    "lastRefillMillis"
)

local tokens = tonumber(bucket[1])
local lastRefillMillis = tonumber(bucket[2])

if tokens == nil then
    tokens = capacity
    lastRefillMillis = nowMills
end

local elapsedMinutes =
    (nowMills - lastRefillMillis) / 60000

local tokensToAdd =
    elapsedMinutes * refillPerMinute

tokens = math.min(
    capacity,
    tokens + tokensToAdd
)

local allowed = 0

if tokens >= 1 then
    tokens = tokens - 1
    allowed = 1
end

redis.call(
    "HSET",
    key,
    'tokens',
    tokens,
    'lastRefillMillis',
    lastRefillMillis
)

redis.call(
    "EXPIRE",
    key,
    ttlSeconds
)

return allowed
