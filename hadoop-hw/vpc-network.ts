import * as gcp from "@pulumi/gcp";
import { usRegion, sgRegion, nameNodeNetworkTag } from './config'

// Create network and subnet
export const network = new gcp.compute.Network('hadoop-vpc', { autoCreateSubnetworks: false })

// Create subnets in Singapore, Iowa, Netherlands, Toronto, Mumbai
const location = ['singapore', 'iowa', 'netherlands', 'toronto', 'mumbai']
export const subnets = ['asia-southeast1', 'us-central1', 'europe-west4', 'northamerica-northeast2', 'asia-south1'].map((region, i) => {
    const zone = region + '-a'
    const subnet = new gcp.compute.Subnetwork(region + '-subnet', {
        ipCidrRange: `10.0.${i+1}.0/24`,
        network: network.id,
        region: region
    })
    return {
        zone, 
        region,
        subnet,
        location: location[i],
        perferedIp: `10.0.${i+1}.5`,
    }
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
    targetTags: [...nameNodeNetworkTag]
})

const iapIngressFirewall = new gcp.compute.Firewall('allow-iap-ingress', {
    network: network.id,
    allows: [
        {
            protocol: 'tcp',
            ports: ['22', '3389'],
        }
    ],
    sourceRanges: ['35.235.240.0/20'],
    direction: 'INGRESS',
})