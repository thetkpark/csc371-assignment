import * as pulumi from "@pulumi/pulumi";
import * as gcp from "@pulumi/gcp";

// // Create a GCP resource (Storage Bucket)
// const bucket = new gcp.storage.Bucket("my-bucket");

// // Export the DNS name of the bucket
// export const bucketName = bucket.url;

// Defined constant variables
const sgRegion = 'asia-southeast1'
const usRegion = 'us-central1'

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


const createInstance = (id: number, region: string, network: gcp.compute.Network, subnet: gcp.compute.Subnetwork ,isMaster: boolean = false) => {
    const name = isMaster ? `hadoop-namenode-${id}` : `hadoop-datanode-${id}`
    return new gcp.compute.Instance(name, {
        zone: region,
        machineType: 'e2-standard-2',
        networkInterfaces: [{
            network: network.id,
            accessConfigs: [{}],
            subnetwork: subnet.id,
        }],
        bootDisk: {
            initializeParams: {
                image: 'projects/ubuntu-os-cloud/global/images/ubuntu-1804-bionic-v20211103',
                size: 20
            }
        },
        tags: isMaster ? ['hadoop-master'] : ['hadoop-datanode'],
    })
}

const namenodeInstances = [createInstance(1, `${sgRegion}-a`, network, sgSubnet, true), createInstance(2, `${usRegion}-a`, network, usSubnet,true)]