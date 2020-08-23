FROM nginx
RUN rm -fr /etc/nginx/conf.d
#RUN rm /etc/nginx/conf.d/default.conf && rm /etc/nginx/nginx.conf

COPY nginx.conf /etc/nginx/nginx.conf
#COPY default.conf /etc/nginx/conf.d/default.conf
COPY *.js /etc/nginx/

RUN mkdir -p /spool/logs

#RUN \
#	apt-get install nginx-plus-module-njs

