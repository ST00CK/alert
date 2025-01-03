FROM jenkins/inbound-agent:3273.v4cfe589b_fd83-1
USER root
RUN apt-get update && apt-get install -y sudo
USER jenkins
