import * as pulumi from "@pulumi/pulumi"
import * as gcp from "@pulumi/gcp"
import { readFileSync } from "fs"
import { network, sgSubnet, usSubnet } from "./vpc-network"
import {
	machineType,
	masterNetworkTag,
	workerNetworkTag,
	sgRegion,
	usRegion,
	usZone,
	sgZone
} from "./config"

const startupScript = readFileSync("./startup.sh", 'utf-8')
// const startupScript = `#!/bin/bash
// sudo apt-get update && sudo apt-get -y dist-upgrade
// sudo apt-get -y install openjdk-8-jdk-headless
// mkdir hadoop-install && cd hadoop-install
// wget https://www.apache.org/dyn/closer.cgi/hadoop/common/hadoop-3.3.1/hadoop-3.3.1-aarch64.tar.gz
// tar xvzf hadoop-3.3.1-aarch64.tar.gz`

const createInstance = (
	id: number,
	region: string,
	network: gcp.compute.Network,
	subnet: gcp.compute.Subnetwork,
	isMaster: boolean = false
) => {
	const name = isMaster ? `hadoop-namenode-${id}` : `hadoop-datanode-${id}`
	return new gcp.compute.Instance(name, {
		zone: region,
		machineType,
		networkInterfaces: [
			{
				network: network.id,
				accessConfigs: [{}],
				subnetwork: subnet.id,
				networkIp: ''
			}
		],
		bootDisk: {
			initializeParams: {
				image:
					"projects/ubuntu-os-cloud/global/images/ubuntu-1804-bionic-v20211103",
				size: 20
			}
		},
		tags: isMaster ? [...masterNetworkTag] : [...workerNetworkTag],
		metadataStartupScript: startupScript
	})
}

const namenodeInstances = [
	createInstance(1, sgZone, network, sgSubnet, true),
	createInstance(2, usZone, network, usSubnet, true)
]

