# ReSplit

WIP!!!

## Docker instructions

### Building and pushing the image to Docker Hub

```
docker image rm bodlulu/resplit:latest
DOCKER_USERNAME=<your docker hub login> DOCKER_PASSWORD=<your docker hub password> ./gradlew dockerPushImage
```

### Running the image

```
docker pull bodlulu/resplit
docker run 
 -e OPENAI_API_KEY="xxxxxxxxxxxxxx" \
 -e OPENAI_LOG="debug" \
 -e PUBLIC_BASE_URL="https://example.com/resplit" \
 -p <PORT TO LISTEN TO>:8042 \ 
 bodlulu/resplit
```

## Licence

Copyright (C) 2025-present Benoit 'BoD' Lubek (BoD@JRAF.org)

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not,
see http://www.gnu.org/licenses/.
