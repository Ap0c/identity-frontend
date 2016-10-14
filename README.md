# identity-frontend

[![Circle CI](https://circleci.com/gh/guardian/identity-frontend/tree/master.svg?style=shield)](https://circleci.com/gh/guardian/identity-frontend/tree/master)

Web frontend for Sign in and Registration at [theguardian.com](http://theguardian.com).


# Application configuration

Configuration files:
- Environment-specific configuration (`conf/<ENV>.conf`)
- Application configuration (`conf/application.conf`)
- System file with additional properties (`/etc/gu/identity-frontend.conf` or `~/.gu/identity-frontend.conf`)

# Local development

## Hosts

Ensure you have the correct [identity-frontend hosts](https://github.com/guardian/identity-frontend/blob/master/nginx/hosts) included in the /etc/hosts file on your machine

## SSL Certificates & nginx setup

We have valid SSL certificates for thegulocal.com and the subdomains we use for local development.

The certificates for the local subdomain `profile-origin.thegulocal.com` are stored in the AWS S3 Identity bucket and are set up as part of the `identity-frontend.conf` for nginx.

Follow these installation steps to correctly setup nginx and valid SSL certificates locally:

* Make sure you are in the base `identity-frontend` directory

* Make sure you have access to the S3 bucket identity-local-ssl and download them using the [AWS CLI utility](https://aws.amazon.com/cli/) (the following command will download them in your current directory using your Identity profile on AWS):

```bash
mkdir nginxCerts
aws --profile identity s3 cp s3://identity-local-ssl/profile-origin-thegulocal-com-exp2016-11-10-bundle.crt ./nginxCerts 1>/dev/null
aws --profile identity s3 cp s3://identity-local-ssl/profile-origin-thegulocal-com-exp2016-11-10.key ./nginxCerts 1>/dev/null
```

**Note**: If you do not have Janus access to Identity, we can grant your team specific access, which means you would substitute `--profile identity` with e.g. `--profile membership`. Contact the Identity team if you require access to these files.

* Find the configuration folder of nginx by running:

```bash
nginxHome=`nginx -V 2>&1 | grep "configure arguments:" | sed 's/[^*]*conf-path=\([^ ]*\)\/nginx\.conf.*/\1/g'`
```

`echo $nginxHome` should display the name of the folder.

* Create symbolic links for the certificates and identity-frontend configuration file for nginx (note: this might require `sudo`)

```bash
sudo ln -fs `pwd`/nginxCerts/profile-origin-thegulocal-com-exp2016-11-10-bundle.crt $nginxHome/profile-origin-thegulocal-com-exp2016-11-10-bundle.crt
sudo ln -fs `pwd`/nginxCerts/profile-origin-thegulocal-com-exp2016-11-10.key $nginxHome/profile-origin-thegulocal-com-exp2016-11-10.key
sudo ln -fs `pwd`/nginx/identity-frontend.conf $nginxHome/sites-enabled/identity-frontend.conf
```

* Restart nginx:

```bash
sudo nginx -s stop
sudo nginx
```

* Optional - verify that your configuration is set up as expected

    - `ls -la $nginxHome` should show the certificates correctly symlinked to the **full pathname** of the downloaded certificates
    - `ls -la $nginxHome/sites-enabled` should show `identity-frontend.conf`  correctly symlinked to the **full pathname** of `identity-frontend/nginx/identity-frontend.conf`

## Configuration

Install the local configuration file from s3:

```
mkdir -p /etc/gu
aws s3 cp --profile identity s3://gu-identity-frontend-private/DEV/identity-frontend.conf /etc/gu
```

**Note**: If you do not have Janus access to Identity, we can grant your team specific access, which means you would substitute `--profile identity` with e.g. `--profile membership`. Contact the Identity team if you require access to these files.

You should now be able to start the application (`sbt run`), go to [https://profile-origin.thegulocal.com/management/healthcheck](https://profile-origin.thegulocal.com/management/healthcheck) and see a green padlock for your local SSL certificate as well as a 200 response.

## Running the application

Requires:

 - [JDK 8](http://openjdk.java.net)
 - [sbt](http://www.scala-sbt.org)
 - [Node.js 4.x](https://nodejs.org)

To run the application in development mode use:

    ./start-frontend.sh

This command will automatically pull down all dependencies for the Scala app,
and client-side dependencies with Node.js. Sources will automatically be watched,
so making changes locally will result in compile being triggered automatically.

Client side sources will automatically be compiled using the `npm run build` command.

## Development and Contributing
See [CONTRIBUTING.MD](https://github.com/guardian/identity-frontend/blob/master/CONTRIBUTING.md).

## Testing

To run unit tests:

    sbt test

To run functional selenium tests in a browser:

    sbt "project functional-tests" test

Credentials for social functional tests are in private `DEV/identity-frontend.conf`.
