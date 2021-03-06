/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package brooklyn.entity.nosql.cassandra;

import brooklyn.entity.proxying.ImplementedBy;

/**
 * @deprecated since 0.7.0; use {@link CassandraDatacenter} which is equivalent but has
 * a less ambiguous name; <em>Cluster</em> in Cassandra corresponds to what Brooklyn terms a <em>Fabric</em>.
 */
@Deprecated
@ImplementedBy(CassandraClusterImpl.class)
public interface CassandraCluster extends CassandraDatacenter {
}
