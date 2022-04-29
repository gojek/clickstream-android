# Setup Local Env for Starting Raccoon

1. Download docker at `https://docs.docker.com/desktop/mac/install/`
2. Clone Raccoon
```
cd Desktop
git clone git@github.com:odpf/raccoon.git
cd raccoon
```

3. Download docker image for raccoon
```
docker pull odpf/raccoon
```

4. Start `odpf/raccoon` in the docker GUI
5. If you using macbook with apple chip, before running the docker-run, do change the
`docker-compose.yml` and add `platform: linux/x86_64` on each section
   
```
services:
  zookeeper:
    platform: linux/x86_64
    ...
  kafka:
    platform: linux/x86_64
    ...
  cs:
    platform: linux/x86_64
    ...
```

6. Run raccoon along with kafka setup
```
 make docker-run
```



