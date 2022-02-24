JasperCloud Mybatis Framework
多数据源

备注：不适合做分布式事务

``` xml
<dependency>
    <groupId>io.github.jaspercloud</groupId>
    <artifactId>mybatis</artifactId>
    <version>3.1.24</version>
</dependency>
```
## config
```properties
spring.jaspercloud.db.names=db1,db2

#db1 datasource
spring.jaspercloud.datasource.db1.driverClassName=org.postgresql.Driver
spring.jaspercloud.datasource.db1.url=jdbc:postgresql://127.0.0.1:5432/master
spring.jaspercloud.datasource.db1.username=postgres
spring.jaspercloud.datasource.db1.password=postgres
spring.jaspercloud.datasource.db1.defaultAutoCommit=false
spring.jaspercloud.datasource.db1.initialSize=10
spring.jaspercloud.datasource.db1.minIdle=10
spring.jaspercloud.datasource.db1.maxActive=50
spring.jaspercloud.datasource.db1.maxWait=60000
spring.jaspercloud.datasource.db1.timeBetweenEvictionRunsMillis=60000
spring.jaspercloud.datasource.db1.minEvictableIdleTimeMillis=300000
spring.jaspercloud.datasource.db1.validationQuery=select 'x'
spring.jaspercloud.datasource.db1.validationQueryTimeout=60
spring.jaspercloud.datasource.db1.testWhileIdle=true
spring.jaspercloud.datasource.db1.testOnBorrow=false
spring.jaspercloud.datasource.db1.testOnReturn=false
spring.jaspercloud.datasource.db1.removeAbandoned=true
spring.jaspercloud.datasource.db1.removeAbandonedTimeout=1800
#slave s1
spring.jaspercloud.datasource.db1.slaves.s1.url=jdbc:postgresql://127.0.0.1:5432/slave1
spring.jaspercloud.datasource.db1.slaves.s1.defaultAutoCommit=false
#slave s2
spring.jaspercloud.datasource.db1.slaves.s2.url=jdbc:postgresql://127.0.0.1:5432/slave2
spring.jaspercloud.datasource.db1.slaves.s2.defaultAutoCommit=false
#db1 mybatis
spring.jaspercloud.mybatis.db1.basePackages=com.jaspercloud.db1.dao.mapper
spring.jaspercloud.mybatis.db1.mapperLocations=classpath:mapper/db1/*.xml
#db1 ddl
spring.jaspercloud.ddl.db1.name=db1
spring.jaspercloud.ddl.db1.location=classpath:db/db1/*.sql

#db2 datasource
spring.jaspercloud.datasource.db2.driverClassName=org.postgresql.Driver
spring.jaspercloud.datasource.db2.url=jdbc:postgresql://127.0.0.1:5432/db2
spring.jaspercloud.datasource.db2.username=postgres
spring.jaspercloud.datasource.db2.password=postgres
spring.jaspercloud.datasource.db2.defaultAutoCommit=false
spring.jaspercloud.datasource.db2.initialSize=10
spring.jaspercloud.datasource.db2.minIdle=10
spring.jaspercloud.datasource.db2.maxActive=50
spring.jaspercloud.datasource.db2.maxWait=60000
spring.jaspercloud.datasource.db2.timeBetweenEvictionRunsMillis=60000
spring.jaspercloud.datasource.db2.minEvictableIdleTimeMillis=300000
spring.jaspercloud.datasource.db2.validationQuery=select 'x'
spring.jaspercloud.datasource.db2.validationQueryTimeout=60
spring.jaspercloud.datasource.db2.testWhileIdle=true
spring.jaspercloud.datasource.db2.testOnBorrow=false
spring.jaspercloud.datasource.db2.testOnReturn=false
spring.jaspercloud.datasource.db2.removeAbandoned=true
spring.jaspercloud.datasource.db2.removeAbandonedTimeout=1800
#db2 mybatis
spring.jaspercloud.mybatis.db2.basePackages=com.jaspercloud.db2.dao.mapper
spring.jaspercloud.mybatis.db2.mapperLocations=classpath:mapper/db2/*.xml
#db1 ddl
spring.jaspercloud.ddl.db2.name=db2
spring.jaspercloud.ddl.db2.location=classpath:db/db2/*.sql

# ddl文件名: ${version}__${name}.sql
# version: 升级版本号
# name: 项目名(一个数据库可能有多个项目，通过name区分开来进行版本控制)
```
