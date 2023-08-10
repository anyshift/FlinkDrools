## 实时流事件规则匹配处理

#### 项目介绍

使用 Kafka 模拟生产各种数据源，体现 Flink 流式处理的高效与性能，辅以 Drools 的规则引擎进行实时规则的计算。

本项目使用官方推荐的规则热更新方案，将规则文件与实际业务逻辑物理分离，完成了松耦合与提高了扩展性。

----

#### 模块划分

- **Rules**：仅 Drools 规则文件。
- **Stream**：数据流处理，包含 Kafka、Flink 与 Drools 的整合使用。

----

#### 注意事项

规则引擎与业务的解耦需要额外添加一个仓库源，本地搭建私有仓库推荐使用 Nexus。

仓库搭建完毕后，还需配置 Maven 的 settings.xml：

```xml
<servers>
    <!--新添加如下内容-->
    <server>
        <id>nexus</id> <!--随便一个ID名-->
        <username>admin</username> <!--本地仓库的管理员用户名-->
        <password>your-admin-password</password> <!--本地仓库的管理员密码-->
    </server>
</servers>
```

```xml
<profiles>
    <!--新添加如下内容-->
    <profile>
        <id>nexus-profile</id>
        <repositories>
            <!--官方仓库-->
            <repository>
                <id>central</id>
                <name>Maven Repository</name>
                <layout>default</layout> <!--优先到官方仓库找-->
                <url>https://repo1.maven.org/maven2</url>
            </repository>
            <!--私有仓库-->
            <repository>
                <id>nexus</id>
                <name>Nexus Repository</name>
                <url>http://localhost:8091/repository/maven-public/</url>
                <releases>
                    <enabled>true</enabled>
                    <updatePolicy>always</updatePolicy>
                </releases>
                <snapshots>
                    <enabled>true</enabled>
                    <updatePolicy>always</updatePolicy>
                </snapshots>
            </repository>
        </repositories>
    </profile>
</profiles>
```

----

#### 规则热更新

规则文件修改后，要想实现热更新，需要修改规则项目所在的 pom.xml 的版本号，然后执行打包命令。 

**请确保构建的 jar 包文件能够发布到私有仓库或自定义仓库中。**