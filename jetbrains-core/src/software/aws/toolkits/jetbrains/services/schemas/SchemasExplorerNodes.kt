// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.schemas

import com.intellij.openapi.project.Project
import icons.AwsIcons
import software.amazon.awssdk.services.schemas.SchemasClient
import software.amazon.awssdk.services.schemas.model.RegistrySummary
import software.aws.toolkits.jetbrains.core.explorer.filters.CloudFormationResourceNode
import software.aws.toolkits.jetbrains.core.explorer.filters.CloudFormationResourceParentNode
import software.aws.toolkits.jetbrains.core.explorer.nodes.AwsExplorerEmptyNode
import software.aws.toolkits.jetbrains.core.explorer.nodes.AwsExplorerNode
import software.aws.toolkits.jetbrains.core.explorer.nodes.AwsExplorerResourceNode
import software.aws.toolkits.jetbrains.core.explorer.nodes.AwsExplorerServiceNode
import software.aws.toolkits.jetbrains.core.explorer.nodes.CacheBackedAwsExplorerServiceRootNode
import software.aws.toolkits.jetbrains.core.explorer.nodes.ResourceParentNode
import software.aws.toolkits.jetbrains.core.getResourceNow
import software.aws.toolkits.jetbrains.services.schemas.resources.SchemasResources
import software.aws.toolkits.resources.cloudformation.AWS
import software.aws.toolkits.resources.message

class SchemasServiceNode(project: Project, service: AwsExplorerServiceNode) :
    CacheBackedAwsExplorerServiceRootNode<RegistrySummary>(project, service, SchemasResources.LIST_REGISTRIES),
    CloudFormationResourceParentNode {
    override fun displayName(): String = message("explorer.node.schemas")
    override fun toNode(child: RegistrySummary): AwsExplorerNode<*> = SchemaRegistryNode(nodeProject, child)
    override fun cfnResourceTypes() = setOf(AWS.EventSchemas.Schema, AWS.EventSchemas.Registry)
}

open class SchemaRegistryNode(
    project: Project,
    val registry: RegistrySummary
) : AwsExplorerResourceNode<RegistrySummary>(
    project,
    SchemasClient.SERVICE_NAME,
    registry,
    AwsIcons.Resources.SCHEMA_REGISTRY
),
    ResourceParentNode,
    CloudFormationResourceNode,
    CloudFormationResourceParentNode {
    override fun resourceType() = "registry"

    override fun resourceArn(): String = value.registryArn() ?: value.registryName()
    override val resourceType = AWS.EventSchemas.Registry
    override val cfnPhysicalIdentifier: String = registry.registryArn()
    override fun cfnResourceTypes() = setOf(AWS.EventSchemas.Schema)

    override fun toString(): String = value.registryName()

    override fun displayName(): String = value.registryName()

    override fun isAlwaysLeaf(): Boolean = false

    override fun isAlwaysShowPlus(): Boolean = true

    override fun getChildren(): List<AwsExplorerNode<*>> = super<ResourceParentNode>.getChildren()

    override fun getChildrenInternal(): List<AwsExplorerNode<*>> {
        val registryName = value.registryName()
        return nodeProject
            .getResourceNow(SchemasResources.listSchemas(registryName))
            .map { schema -> SchemaNode(nodeProject, schema.toDataClass(registryName)) }
            .toList()
    }

    override fun emptyChildrenNode(): AwsExplorerEmptyNode = AwsExplorerEmptyNode(
        nodeProject,
        message("explorer.registry.no.schema.resources")
    )
}

open class SchemaNode(
    project: Project,
    val schema: Schema
) : AwsExplorerResourceNode<Schema>(
    project,
    SchemasClient.SERVICE_NAME,
    schema,
    AwsIcons.Resources.SCHEMA
),
    CloudFormationResourceNode {
    override fun resourceType() = "schema"

    override fun resourceArn() = cfnPhysicalIdentifier
    override val resourceType = AWS.EventSchemas.Schema
    override val cfnPhysicalIdentifier = value.arn ?: value.name

    override fun toString(): String = value.name

    override fun displayName() = value.name
}
