
# clear the mongodb charts data folder, it will be recreated upon startup
echo 'clearing old mongodb charts data...'
rm -rf mongo-charts-data/

mkdir -p mongo-charts-data/keys

echo 'starting docker kafka cluster...'
docker-compose up -d

echo 'pausing for 60 seconds before adding charts user while mongo-charts boots up...'
sleep 60s

echo 'adding user charts@centene.com to mongo charts...'
docker exec -it \
  $(docker container ls --filter name=_charts -q) \
  charts-cli add-user --first-name "Charts" --last-name "User" \
  --email "charts@centene.com" --password "password" \
  --role "UserAdmin"
