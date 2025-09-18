##### Stage 1: Build WAR bằng Maven #####
FROM git.vn/base-image-local/base/maven:3.8.3-openjdk-17 AS build
WORKDIR /app

# Cache Maven repo để build nhanh hơn
RUN --mount=type=cache,target=/root/.m2 mvn -v

# Copy source code + lib cần thiết
COPY CommonLib ./CommonLib
COPY jHTTP ./jHTTP
COPY JCaches ./JCaches
COPY jSQL ./jSQL
COPY DataApi ./DataApi

# Build tất cả modules (gom lại 1 RUN để giảm layer)
RUN cd CommonLib && mvn clean install -DskipTests && cd .. 
RUN cd jHTTP && mvn clean install -DskipTests && cd ..  
RUN cd JCaches && mvn clean install -DskipTests && cd .. 
RUN cd jSQL && mvn clean install -DskipTests && cd .. 
RUN cd DataApi && mvn clean package -DskipTests && cd ..

##### Stage 2: Deploy lên Tomcat #####
FROM git.vn:6666/base-image-local/base/tomcat:9.0.107-jdk8-corretto
WORKDIR /usr/local/tomcat

# Set timezone
RUN ln -snf /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime

# Copy WAR từ stage build sang Tomcat
COPY --from=build /app/DataApi/target/*.war ./webapps/DATAAPI.war

#Tạo user non-root cho Tomcat
#RUN addgroup --system --gid 1002 tomcat \
# && adduser --system --uid 1002 --gid 1002 tomcat \
# && chown -R tomcat:tomcat /usr/local/tomcat

#USER tomcat
EXPOSE 8080
CMD ["catalina.sh", "run"]