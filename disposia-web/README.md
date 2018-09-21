# EchoWeb

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 1.6.3.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

Alternatively, there is a Gradle task to run this too:

```
gradle ngServe
```

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `-prod` flag for a production build.

### Docker image

After a production build, a Docker image can be built with:

```
docker build -t echo-web:latest . 
docker run -p 80:80 --name echo-web echo-web 
```

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).


## CORS proxy error

There is some weired error because the backend is not bound to `localhost:4200` as `ng serve` expects. A config for this is in `proxy.config.json`.

Run therefore with:

```
ng serve --proxy-config proxy.config.json
```

More info, see [Proxy To Backend](https://github.com/angular/angular-cli/blob/master/docs/documentation/stories/proxy.md)
