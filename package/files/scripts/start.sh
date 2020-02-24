#!/bin/sh

set -e  

# if $variable is not set, then default to $value
export REDIS_CACHE_URL=${REDIS_CACHE_URL:-redis://redis:6379/1}
export REDIS_CACHE_REPLICA=${REDIS_CACHE_REPLICA:-redis://redis:6379/2}

export REDIS_MESSAGE_CACHE_URL=${REDIS_MESSAGE_CACHE_URL:-redis://redis:6379/3}
export REDIS_MESSAGE_CACHE_REPLICA=${REDIS_MESSAGE_CACHE_REPLICA:-redis://redis:6379/4}

export REDIS_DIRECTORY_URL=${REDIS_DIRECTORY_URL:-redis://redis:6379/5}
export REDIS_DIRECTORY_REPLICA=${REDIS_DIRECTORY_REPLICA:-redis://redis:6379/6}

export REDIS_PUSH_SCHEDULER_URL=${REDIS_PUSH_SCHEDULER_URL:-redis://redis:6379/7}
export REDIS_PUSH_SCHEDULER_REPLICA=${REDIS_PUSH_SCHEDULER_REPLICA:-redis://redis:6379/8}


export PUSH_QUEUE_SIZE=${PUSH_QUEUE_SIZE:-1024}


export MINIO_ATTACHMENTS_ACCESS_KEY=${MINIO_ATTACHMENTS_ACCESS_KEY:-minioadmin}
export MINIO_ATTACHMENTS_ACCESS_SECRET=${MINIO_ATTACHMENTS_ACCESS_SECRET:-minioadmin}
export MINIO_ATTACHMENTS_BUCKET=${MINIO_ATTACHMENTS_BUCKET:-bucket-for-attachments}

export MINIO_PROFILES_ACCESS_KEY=${MINIO_PROFILES_ACCESS_KEY:-minioadmin}
export MINIO_PROFILES_SECRET=${MINIO_PROFILES_SECRET:-minioadmin}
export MINIO_PROFILES_BUCKET=${MINIO_PROFILES_BUCKET:-bucket-for-profile}
export MINIO_PROFILES_REGION=${MINIO_PROFILES_REGION:-eu-west-1}


export POSTGRES_DATABASE_DRIVER_CLASS=${POSTGRES_DATABASE_DRIVER_CLASS:-org.postgresql.Driver}
export POSTGRES_DATABASE_USER=${POSTGRES_DATABASE_USER:-postgres}
export POSTGRES_DATABASE_PASSWORD=${POSTGRES_DATABASE_PASSWORD:-test123test123}
export POSTGRES_DATABASE_URL=${POSTGRES_DATABASE_URL:-jdbc:postgresql://postgres:5432/accountdb}

export POSTGRES_MESSAGE_STORE_DRIVER_CLASS=${POSTGRES_MESSAGE_STORE_DRIVER_CLASS:-org.postgresql.Driver}
export POSTGRES_MESSAGE_STORE_USER=${POSTGRES_MESSAGE_STORE_USER:-postgres}
export POSTGRES_MESSAGE_STORE_PASSWORD=${POSTGRES_MESSAGE_STORE_PASSWORD:-test123test123}
export POSTGRES_MESSAGE_STORE_URL=${POSTGRES_MESSAGE_STORE_URL:-jdbc:postgresql://postgres:5432/messagedb}


export APN_BUNDLE_ID=${APN_BUNDLE_ID:-com.secret.signalTest}
export APN_PUSH_CERTIFICATE=${APN_PUSH_CERTIFICATE:-/home/wire/signal/Signal-Server/config/signal.p12}
export APN_PUSH_KEY=${APN_PUSH_KEY:-123456}


export FCM_SENDER_ID=${FCM_SENDER_ID:-947045074136}
export FCM_API_KEY=${FCM_API_KEY:-AIzaSyCH0VC5Gkwm_X0gNyq486el7KniqriCvIQ}


export APP_SERVER_TYPE=${APP_SERVER_TYPE:-http}
export APP_SERVER_PORT=${APP_SERVER_PORT:-9002}

export ADMIN_SERVER_TYPE=${ADMIN_SERVER_TYPE:-http}
export ADMIN_SERVER_PORT=${ADMIN_SERVER_PORT:-9003}


export

echo "*************************************************************"
echo "Creating config files using confd"
echo "*************************************************************"
/usr/local/bin/confd -onetime -backend env

echo "*************************************************************"
echo "Generated system configuration"
echo "*************************************************************"
cat /config/sample1.yml

echo "*************************************************************"
echo "Creating output directory"
echo "*************************************************************"
mkdir -p $POSTALREGISTRATION_OUTPUT_DIR

echo "*************************************************************"
echo "Migrate databases"
echo "*************************************************************"
java -jar /tmp/TextSecureServer-1.88.jar accountdb migrate /config/sample1.yml
java -jar /tmp/TextSecureServer-1.88.jar messagedb migrate /config/sample1.yml

echo "*************************************************************"
echo "Starting Signal Service"
echo "*************************************************************"
java -jar /tmp/TextSecureServer-1.88.jar server /config/sample1.yml -s