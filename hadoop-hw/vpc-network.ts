import * as gcp from "@pulumi/gcp";
import { usRegion, sgRegion, masterNetworkTag } from './config'

// Create network and subnet
export const network = new gcp.compute.Network('hadoop-vpc', { autoCreateSubnetworks: false })
export const sgSubnet = new gcp.compute.Subnetwork('singapore-subnet', {
    ipCidrRange: '10.0.1.0/24',
    network: network.id,
    region: sgRegion,
})
export const usSubnet = new gcp.compute.Subnetwork('america-subnet', {
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
    targetTags: [...masterNetworkTag]
})