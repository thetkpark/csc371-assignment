import * as pulumi from "@pulumi/pulumi"
import * as gcp from "@pulumi/gcp"
import { readFileSync } from "fs"
import { network, subnets } from "./vpc-network"
import {
	machineType,
	nameNodeNetworkTag,
	dataNodeNetworkTag,
	project,
} from "./config"

const startupScript = readFileSync("./startup.sh", 'utf-8')

const createInstance = (
	id: number,
	network: gcp.compute.Network,
	subnet: gcp.compute.Subnetwork,
	zone: string,
	location: string,
	privateIp: string,
	isNamenode: boolean = false
) => {
	const name = (isNamenode ? `hadoop-${id}-namenode` : `hadoop-${id}-datanode`) + `-${location}`
	return new gcp.compute.Instance(name, {
		zone,
		machineType,
		networkInterfaces: [
			{
				network: network.id,
				accessConfigs: [{}],
				subnetwork: subnet.id,
				networkIp: privateIp
			}
		],
		bootDisk: {
			initializeParams: {
				image:
					"projects/ubuntu-os-cloud/global/images/ubuntu-1804-bionic-v20211103",
				size: 20
			}
		},
		tags: [...dataNodeNetworkTag].concat(isNamenode ? nameNodeNetworkTag : []),
		metadataStartupScript: startupScript,
		project,
	})
}

const instances = subnets.map((subnet, i) => {
	createInstance(i+1, network, subnet.subnet, subnet.zone, subnet.location, subnet.perferedIp, i < 2)
})
