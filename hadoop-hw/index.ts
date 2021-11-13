import * as pulumi from "@pulumi/pulumi";
import * as gcp from "@pulumi/gcp";

// // Create a GCP resource (Storage Bucket)
// const bucket = new gcp.storage.Bucket("my-bucket");

// // Export the DNS name of the bucket
// export const bucketName = bucket.url;

// Defined constant variables
const sgRegion = 'asia-southeast1-a'
const usRegion = 'us-central1-a'

// Create network and subnet
const network = new gcp.compute.Network('hadoop-vpc', { autoCreateSubnetworks: false })
const sgSubnet = new gcp.compute.Subnetwork('singapore-subnet', {
    ipCidrRange: '10.0.1.0/24',
    network: network.id,
    region: sgRegion,
})
const usSubnet = new gcp.compute.Subnetwork('america-subnet', {
    ipCidrRange: '10.0.2.0/24',
    network: network.id,
    region: usRegion,
})

// Create firewall rule
const hadoopMasterFirewall = new gcp.compute.Firewall('allow-hadoop-master-management-port', {
    network: network.id,
    allows: [
        {
            protocol: 'tcp',
            ports: ['9870'],
        }
    ],
    targetTags: ['hadoop-master']
})


const createInstance = (id: number, region: string, network: gcp.compute.Network ,isMaster: boolean = false) => {
    const name = isMaster ? `hadoop-namenode-${id}` : `hadoop-datanode-${id}`
    return new gcp.compute.Instance(name, {
        zone: region,
        machineType: 'e2-standard-2',
        networkInterfaces: [{
            network: network.id,
            name: isMaster ? `hadoop-namenode-${id}-nic` : `hadoop-datanode-${id}-nic`,
            accessConfigs: [{}],
            subnetwork: sgSubnet.id,
        }],
        bootDisk: {
            initializeParams: {
                image: 'ubuntu-os-cloud/ubuntu-1804-bionic-v20200415',
                size: 50
            }
        },
        tags: isMaster ? ['hadoop-master'] : ['hadoop-datanode'],
    })
}

const namenodeInstances = [createInstance(1, sgRegion, network, true), createInstance(2, usRegion, network, true)]