#指定基础镜像(一般指定程序所依赖的环境)
#本例子是构建SpringBoot所以我指定的jdk环境变量
FROM openjdk:8

#添加维护者信息
MAINTAINER author:edison
MAINTAINER mail:2226565664@qq.com

#将./hello-world.jar添加到容器的/opt目录中
COPY ./cloud-eruake.jar /opt

#指定dockerfile的命令在哪个目录下执行
WORKDIR /opt

#指定容器和外界交互的端口
EXPOSE 8001

#容器运行后执行的命令(该命令在WORKDIR指定的工作目录下执行)
#注意：多个CMD指令只有最后一个会生效
CMD java -jar cloud-eruake.jar

#在构建镜像时执行的命令
#我这边用它来打印java的环境变量
RUN java -version

#添加该镜像的元数据
LABEL version="1.0" description="这是一个Web服务器" by="edc"
