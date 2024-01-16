# Use an official Node.js image as the base image
#FROM node:14

# Set the working directory inside the container
#WORKDIR /app

# Copy the package.json and package-lock.json files to the container
#COPY package*.json ./

# Install project dependencies
#RUN npm install

# Copy the rest of the application code to the container
#COPY . .

# Build the Quasar app
#RUN quasar build

# Expose the port that the app will run on
#EXPOSE 80

# Command to start the app
#CMD [ "npm", "run", "start" ]

# develop stage
FROM node:20-alpine3.17 as develop-stage
WORKDIR /app
COPY package*.json ./
RUN yarn global add @quasar/cli
COPY . .
# build stage
FROM develop-stage as build-stage
RUN yarn
RUN quasar build
# production stage
FROM nginx:1.25.2 as production-stage
COPY --from=build-stage /app/dist/spa /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]