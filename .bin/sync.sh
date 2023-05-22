scp target/code-htc-0.0.1.jar root@47.111.158.6:/opt/code-htc

nohup java -jar code-htc-0.0.1.jar --spring.profiles.active=prod --server.port=10251 > output.log>&1 &