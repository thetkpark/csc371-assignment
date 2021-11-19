# Hadoop

## Cluster Information

- Username: `hadoop`
- Password: `Qp2SDkywCwtsgACTt28&a%CJdUU2PA`

### Network Configuration

- Primary NameNode + DataNode 1
  - IP: 10.0.1.5
  - Region: asia-southeast1
  - Location: Singapore
- Secondary NameNode + DataNode 2
  - IP: 10.0.2.5
  - Region: us-central1-a
  - Location: Iowa
- DataNode 3
  - IP: 10.0.3.5
  - Region: europe-west4-a
  - Location: Netherlands
- DataNode 4
  - IP: 10.0.4.5
  - Region: northamerica-northeast2
  - Location: Toronto
- DataNode 5
  - IP: 10.0.5.5
  - Region: asia-south1
  - Location: Mumbai

## Cluster installation steps

1. Create the new project in Google Cloud Platform

2. Intialize Pulumi on local machine for handling IoC (Infrastructure as Codes).

3. Write the code in TypeScript to create VPC Network, subnets, firewall rules, and 5 virtual machine with fixed internal IP address.

4. Run the Pulumi CLI to let it create the resources on desired GCP project.

5. SSH into each of the virtual machines to install JDK version 8.

6. Create `hadoop` user in each vm

6. Create new direcotry for storing files in HDFS with `sudo mkdir -p /usr/local/hadoop/hdfs/data` in every VM.

6. Change ownership to the `hadoop` user using `sudo chown -R hadoop:hadoop /usr/local/hadoop/hdfs/data`

7. Download the binary of Hadop 3.3.1 and extract the archive in each VM.

8. Update `etc/hadoop/hadoop-env.sh` on all nodes to export the necessary environment variable

   ```sh
   export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
   export HDFS_NAMENODE_USER="hadoop"
   export HDFS_DATANODE_USER="hadoop"
   export HDFS_SECONDARYNAMENODE_USER="hadoop"
   export YARN_RESOURCEMANAGER_USER="hadoop"
   export YARN_NODEMANAGER_USER="hadoop"
   export HADOOP_HOME=/home/hadoop/hadoop-3.3.1
   ```

9. Update `etc/hadoop/core-site.xml` on on all nodes

   ```xml
   <configuration>
       <property>
           <name>fs.defaultFS</name>
           <value>hdfs://10.0.1.5:9000</value>
       </property>
   </configuration>
   ```

10. Update `etc/hadoop/hdfs-site.xml` on all nodes 

    ```xml
    <configuration>
        <property>
            <name>dfs.replication</name>
            <value>3</value>
        </property>
        <property>
            <name>dfs.datanode.data.dir</name>
            <value>file:///usr/local/hadoop/hdfs/data</value>
        </property>
    </configuration>
    ```

11. For the primary NameNode,  add the secondary NameNode configuration in `etc/hadoop/hdfs-site.xml`

    ```XML
    <property>
      <name>dfs.namenode.secondary.http-address</name>
      <value>10.0.2.5:50090</value>
    </property>
    ```

12. Update `etc/hadoop/cat mapred-site.xml` on all primary namenode 

    ```xml
    <configuration>
    <property>
        <name>mapreduce.jobtracker.address</name>
        <value>10.0.1.5:54311</value>
      </property>
      <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
      </property>
      <property>
        <name>yarn.nodemanager.vmem-check-enabled</name>
        <value>false</value>
      </property>
      <property>
        <name>yarn.app.mapreduce.am.env</name>
        <value>HADOOP_MAPRED_HOME=${HADOOP_HOME}</value>
      </property>
      <property>
        <name>mapreduce.map.env</name>
        <value>HADOOP_MAPRED_HOME=${HADOOP_HOME}</value>
      </property>
      <property>
        <name>mapreduce.reduce.env</name>
        <value>HADOOP_MAPRED_HOME=${HADOOP_HOME}</value>
      </property>
    </configuration>
    ```

13. On primary namenode, update `etc/hadoop/yarn-site.xml`

    ```xml
    <configuration>
        <property>
            <name>yarn.nodemanager.aux-services</name>
            <value>mapreduce_shuffle</value>
        </property>
        <property>
            <name>yarn.nodemanager.aux-services.mapreduce.shuffle.class</name>
            <value>org.apache.hadoop.mapred.ShuffleHandler</value>
        </property>
        <property>
            <name>yarn.resourcemanager.hostname</name>
            <value>10.0.1.5</value>
        </property>
    </configuration>
    ```

14. On primary namenode, add IP addresses of all datanodes to `etc/hadoop/workers`

    ```
    localhost
    10.0.2.5
    10.0.3.5
    10.0.4.5
    10.0.5.5
    ```

15. On primary namenode, add IP addresses of all primary namenode to `etc/hadoop/masters`

    ```
    10.0.1.5
    ```

18. Generate private key and public key on each node with `ssh-keygen` command

19. Copy the public key of primary namenode to `~/.ssh/authorized_keys` on every nodes.

20. Format the HDFS with `sudo ./bin/hdfs namenode -format`

21. Use the script `sbin/start-all.sh` to start the Hadoop cluster.

