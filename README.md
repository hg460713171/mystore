# **基于istio下一代微服务架构**
## **前言（必读）**

这本书包含了如何从零开发一个istio微服务系统，并且提供了整套开发流程，包括开发/测试/上线的流程，让人在k8s环境下开发如同普通的本地开发，并提供如何尽量少损失的从传统的微服务+mysql迁移到istio + tidb的架构。


前几年有关微服务，分布式的思想基本充斥着整个it界，微服务包含了注册中心模块，配置中心模块，RPC通信模块，服务治理模块，监控模块，网关安全等。其实系统复杂性增加了很多，这就促使了k8s的流行。k8s作为云原生最核心的组件，提供了非常多的功能与服务TODO, 服务的scale out变得异常简单 开发人员完全可以替代运维人员，
但在站在k8s中回头考虑微服务，我们真的需要臃肿的微服务架构吗

  功能  |k8s | istio|  dubbo | springcloud
--- |:--- | :---   | :---  | :---
 弹性伸缩 | autoscaling     |利用autoscaling|  无|无
服务注册发现 | k8s DNS     | sidecar | zk&nacos|  eureka
配置中心 | configmap     | configmap| Apollo | springcloud config
网关 | ingress     | istio-gateway| zk&nacos|  zuul
负载均衡 | kubeproxy    | sidecar| dubbo |  ribbon
服务治理 |     | sidecar| dubbo |  config
安全（指的是微服务之间的调用权限控制 不是登录） | RBAC    | 利用RBAC| 无也不需要(因为外部访问不了) |  SpringSecurity + 网关
熔断 |     | sidecar| dubbo |  hystrix
降级 |     | 不支持（下文会给简易的解决方案） | dubbo(mock) |  hystrix(fallback)
rpc协议 |     | http1.1/go（基于http2）| dubbo3.0后基于http2 |  http1.1/http2



istio的很大的优势在于：**不需要自建注册中心和配置中心，而是由istio利用k8s本身的特性以及etcd来实现。 服务的治理则是sidecar通过k8s提供的各种特性进行管理，基层开发人员可以专注于业务的开发。对于中小型项目来说是个很好的优势**



劣势则是在于：**技术骨干需要学习的东西过多，而且有时候k8s和istio 提供的服务较为简陋，需要自己对框架进行自研扩展。大型架构需要对dubbo等中间件进行重写或者扩展，而istio本意是通过配置去管控，但是sidecar基本都是go或者c的架构，对java工程师极其不友好，不过和nginx类似,lua脚本可以用在EnvoyFilter中，只要不是极端复杂的场景，基本都可以满足**



**我个人对istio的使用configmap 和etcd等是非常赞同的，但是说实话我并不喜欢sidecar服务治理的设计，有种脱了裤子放屁的味，我更喜欢的是dubbo的思路，嵌入sdk，并给出spi接口进行扩展。**

下文也会对这几点进行介绍

有关http1.1 和http2的区别可以看文章  TODO


ps：搭建过程中肯定会遇到各种各样的小问题，推荐全程翻墙,而且云原生时代，架构更迭太快，中英文互联网blog上的信息基本都过时了，所以如果有官网教程的东西，我会附上官网链接，不会再copy下来，如果没有中文官网的，我会写上一些基本的用法，但想要会用还是要去读官方文档。
遇到问题去github上issue上查询提问，配合论坛+搜索引擎才能更快的定位到问题。

关于为什么不用dapr(呆破儿)而是istio（伊斯题噢） 主要是考虑老系统的迁移难度，对于springcloud 而言，istio基本是无痛迁移，需要重构的只有网关层（服务治理，路由等业务无关的功能），对dubbo而言 需要重构的也只有网关层+微服务调用方式。而dapr需要改动中间件。

当然也可以采用dubbo协议，但是需要重构dubbo代码 剔除dubbo中的服务发现+服务注册+Mock等过滤机制+自己spi扩展的过滤器+错误重试,这对不熟悉dubbo源码的人来说，还不如直接改用http调用。而且删着删着你会发现，dubbo除了提供个rpcclient和rpcserver外，基本没什么代码了，另外dubbo的错误重试代码是嵌入到rpcCLient和rpcServer里面的，所以改动十分巨大。不如基于http二次开发,更何况dubbo3.0也已经转向http2了。

-------
## **quickstart**
k8s安装
```
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube
alias kubectl="minikube kubectl --"

```
isito安装


## **简易私有云安装以及istio安装**
### **k8安装**

k8s 安装，建议看官方文档，写的很好 
```
https://kubernetes.io/zh-cn/docs/tasks/tools/
```
#### **私有云环境的搭建** 
  
kubeadm 搭建多master的k8s集群就好 中文官网非常详细
```
https://kubernetes.io/zh-cn/docs/tasks/tools/
```
ps：生产环境的私有云涉及到非常多的底层网络知识，比如CNI 容器通信,ARP和BGP等广播路由协议,正常的开发基本只接触tcp和http，这种传输层,应用层的知识,对2层/3层协议涉及很少，如果运维搞不来,建议直接钞能力。


**私有云 对外暴露service(type=loadbalancer)类型**

私有云是没有loadbalancer实现的 有钱的可以搞F5 自己搭建用开源的metallb就可以

metallb的原理主要是提供负载均衡+提供外部访问地址。
提供外部访问地址的原理是 暴露局域网内预留的地址，通过arp等协议通知局域网。

**私有云 对外暴露service(type=ingress)类型**

私有云是没有ingress实现的 ，可以使用nginx Ingress Controller
Ingress 粒度很粗，所以istio并没有采用ingress的方案，我自己就不狗尾续貂了，直接采用人家给的方案。

```
https://metallb.universe.tf/configuration/
```
#### **单机本地云环境的搭建**
minikube/kind/k3s都可以 

我是用的minikube

https://kubernetes.io/zh-cn/docs/tasks/tools/

本地loadbalancer 直接用minikube tunnel 暴露地址即可，或者port-forward暴露端口


### **istio的安装**
istio安装：
istio 有自己的istioctl命令，也可以通过helm,也可以通过mainifest，生产上使用尽量通过mainifest，才知道k8s做了什么。
```
https://istio.io/latest/zh/docs/setup/install/istioctl/
```
安装完后会出现 TODO

建议跟着官网走一遍quickstart（入门），想要深入了解就跟着官网走一遍task（任务）,文档写的非常好，值得仔细研读。

## **架构总览**
![11.png](static%2F11.png)
## **开发以及上线**
### **开发环境以及开发工具的搭建**


istio 开发环境搭建
istio 强依赖与k8s环境，与以往开发模式不同
这就要求开发人员一定要做好单元测试，确保提交到开发环境的docker镜像是基本可用的。

提供几种开发环境的设置

1. 每人一个minikube/k3s/kind环境

   公共k8s提供中间件服务并把端口port-forward出来,使用staffold进行CI/CD更新自己的环境 开发时把k8spod的端口通过telepresence转发给本地应用。此时每人的电脑上都有一份最小的系统环境。无论怎么搞都影响不了其他人。但自己需要维护自己的k8s环境，对开发人员要求高。但是一旦成型，每个人都是devops engineer+software developer,等核心成员熟悉以后，就可以被毕业，对外输送人才了。



2. 每人一个minikube/k3s/kind环境
   
   直接使用google cloud code 插件，一键run/debug  我在国外用的挺好,回国发现需要翻墙-_-||
   而且变更代码 需要重新打镜像，速度不如第一种块
   
   第一种方案： 等于把k8s的端口代理到本地端口，和以前的开发基本一样
   
   第二种方案： 等于把k8s在你需要的实时打docker镜像，并通过remoteDebug进行debug,
   有关remotedebug 其实就是一种jvm的协议，IDE通过这个协议和JVM进行通信,IDE告诉你停，你就要停。
   
   https://blog.csdn.net/Trouvailless/article/details/125018972

    **我个人感觉不是特别合适，对电脑性能和网络要求高，但是是google也在用的开发模式**


3. 公用k8s环境，每个开发分配一个namespace+公共中间件环境
   
   共用中间件，管理员分配每个开发分配一个namespace，和一个前端测试专用的namespace，大家互不干扰。通过RBAC控制namespace权限。



4. 公用k8s环境，搭建一个dev后端环境+公共中间件环境 
   
   其他人只需要在本机运行微服务,k8spod的端口通过telepresence转发给本地应用, 但是需要对开发数据进行染色 ，每人一个测试账号，通过http header进行染色，通过istio的sidecar进行流量的分割 (k8s的负载均衡是做不到的,因为k8s只提供四层负载均衡，而对header进行解析需要提供7层的服务)，类似与dubbo中不同的version。但是管理人员比较累，适合大部分是外包的工作环境。


5. 如果实在因为某些原因无法上k8s到开发环境，也可以本地启项目（因为服务治理并不影响RPC的调用，而可以指定本机ip作为服务发现的地址），这时候就等价于springboot+httpClient+httpServer+自动把request和response序列化为实体类。但还是需要至少1-2个核心成员进行istio的管控部署一个公共开发环境。 
  

**实际生产中可以结合多种开发模式，比如核心成员使用k8环境,确保代码和docker镜像的准确性，核心成员以下采用local环境进行开发，**




最重要的开发工具 官方唯一推荐telepresence安装下载

```
https://www.getambassador.io/docs/telepresence/latest/install
```
另外还有 nocalhost这种，有兴趣的可以自己查资料

```
TODO 如何启动项目
```
安装完后TODO

现在2022以后的idea已经支持telePresence 插件，idea yyds

### **测试环境**
#### **基于JVM remoteDebug远程调试k8s测试环境**
https://www.cnblogs.com/wkynf/p/15115295.html

#### **通过染色流量实现把测试环境**


### 生产环境





## 如何从传统微服务转移到istio架构以及与传统微服务的对比
### isito的缺陷以及如何弥补
1. 微服务 
   自研是非常简单的 
   okhttp  

2. 断路器 
   断路器交给前端去处理

   有关断路器的详情 https://www.exoscale.com/syslog/istio-vs-hystrix-circuit-breaker/


3. 配置中心热更新
   
   通过k8s 

### 前端篇

#### 前端容器的选择
#### 
#### 主要介绍一下nginx 

#### 自实现前端容器（需要简单的netty和多线程相关的知识 不需要看懂netty源码）

#### 前端开发环境的搭建

### java篇（jvm参数的配置）

### 微服务篇

#### 迁移方案
1. dubbo 迁移起来还是比较复杂的，没必要这么做 
2. springcloud 基本无损迁移


#### 服务注册   
#### 服务发现
3种服务发现
1 本地
2 无sidecar
3 sidecar
#### 服务治理
##### 服务降级
#### 分布式事务
istio 是不支持服务降级的，但是

#### 自实现rpc （需要简单的netty和多线程相关的知识 不需要看懂netty源码）
#### 自实现注册中心


### 数据库篇

#### mysql和分布式数据库（tidb）

#### 分库分表以及分布式事务

### 中间件的选择
#### es 
#### redis
1. 多级缓存,内存缓存
2. 如何查询命中率
   通过redis事件
3. 如何优化命中率
   多级缓存 + 复制多个
#### mq
#### Prometheus

#### 日志收集平台
1. logfile - filebeat - logstash - es - kibana
  
   **性能差，实时性不好，对系统无侵入性，搭建简单，logstash性能差，消息积压后会崩溃，适合中型流量**
   
2. log4j - kafka - flume - es - kibana
   
   **性能好,实时性有时候不好（flume的jvm 经常full gc），对系统有侵入性，适合大型流量**

3. 接入公司统一的日志管理平台

###  高并发的思考

#### 多级缓存
