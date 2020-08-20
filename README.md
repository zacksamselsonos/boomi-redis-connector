# Redis Connector for Dell Boomi

The Redis Connector for Dell Boomi is a custom connector implementation that enables connectivity to Redis standalone and clustered instances. 

## Purpose
Dell Boomi currently has no solution for a caching layer that persists between process executions. The Redis Connector allows developers to cache the results of API requests and expensive I/O operations inside of a Redis database. Any process execution can then use the contents of the Redis cache to pull cached data instead of performing expensive and duplicative processing.

## Capabilities
The connector can be used to store any data structure in a Redis database, and currently supports the following Redis data types and operations:

|Redis Data Type|Boomi Operation|TTL Support|Remarks|
|-|-|-|-|
|String|GET|Read||
|String|UPSERT|Write||
|String|DELETE|||
|HashSet|GET|Read|GET operations will return the entire hashset. There is currently no way to query for items within a hashset using a connector operation.|
|HashSet|UPSERT|Write||
|HashSet|DELETE||DELETE operations will delete the entire hashset. There is currently no way to delete specific items within a hashset using a connector operation.|

## Getting Started
Installation of a custom connector for Dell Boomi is relatively simple, but it will require Dell Boomi account administrator access to upload and release the connector files.

### Download the latest release

Download the latest Redis Connector release from the [Releases](https://github.com/zachary-samsel/boomi-redis-connector/releases) page.

### Extract the connector
Extract the release zip file into an accessible location. You should now see the following files:

* RedisConnector-\<version>--car.zip
* connector-descriptor.xml

### Create a Boomi Connector Group
Add a custom connector group to your Boomi account by following the instructions described in the Boomi documentation located here: https://help.boomi.com/bundle/connectors/page/t-atm-Adding_a_connector_group.html

### Upload the connector
Using the files that you extracted from the release download, upload the connector to the new connector group by following the instructions described in the Boomi documentation located here: https://help.boomi.com/bundle/connectors/page/t-atm-Adding_a_version_to_a_connector_group.html

### Upgrading a connector version
You can upgrade your Redis Connector version by clicking the "Add Version" button in your existing Redis Connector connector group.

## Using The Connector
Once you install the connector to your account, you can begin using it like any other application connector on the Boomi Web IDE.

### Creating a connection
The first step is to create a Redis connection component and enter one or more hosts. 

The 'Redis Host(s)' field expects one or more semi-colon delimited Redis URIs. A Redis URI is similar to other database connection string URIs and can be used to store host, authentication, and database index configurations among many other interesting properties. You can find more on Redis URI syntax at https://github.com/lettuce-io/lettuce-core/wiki/Redis-URI-and-connection-details.

You can use the 'Test Connection' button to ensure that your atoms are able to connect to the Redis host(s).

>_Note:_ The cloud or local atom that you select must have network access to the Redis host(s). If you're using AWS Elasticache, the Redis cluster security group is not accessible over the WAN by default. You'll need to use a cloud or local atom with appropriate network access through the security group that has access to the Redis host(s).

### Creating an operation
The steps to create any operation type is the same, although the operation options differ once created. Begin by creating a Redis connector operation component and clicking the Import button to complete the operation creation. The wizard will ask you to select the Redis data type being accessed by the new operation.

### Configuring an operation
Find a description of the different operation properties below:

|Property|Operations|Default|Description|Remarks|
|-|-|-|-|-|
|Key Prefix|GET, UPSERT, DELETE|\<Empty>|Used as a cache key prefix|Key prefix allows developers to create logical cache key taxonomies to help separate caching operations made by multiple application domains.|
|Throw On Not Found|GET|true|When enabled, GET operations for cache keys that do not exist throw an application error. <br><br>When disabled, GET operations for for cache keys that do not exist result in a success with no output documents|By enabling 'Return Application Error Responses', a developer can handle GET failures without the use of a try/catch. However, the connector returns empty documents; any handling of GET failures must be done using dynamic document properties.|

### Document Properties
Find a description of the different document properties used by the connector below:

|Property|Operations|Direction|Required|Description|Remarks|
|-|-|-|-|-|-|
|key|UPSERT|Input|true|Stores the cache key to use when upserting the document data into Redis.|
|ttl|UPSERT|Input|false|Stores cache key time-to-live (ttl/expiration) to use in seconds.|Cache keys do not expire by default. Developers can optionally specify the cache key ttl using this property|
|ttl|GET|Output|false|Stores the current cache key time-to-live in seconds.|When getting a cache key, this output property will store the current ttl in seconds if the cache key is configured with an expiration.|
