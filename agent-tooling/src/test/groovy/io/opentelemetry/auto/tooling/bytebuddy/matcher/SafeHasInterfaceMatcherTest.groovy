/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.opentelemetry.auto.tooling.bytebuddy.matcher

import io.opentelemetry.auto.tooling.bytebuddy.matcher.testclasses.A
import io.opentelemetry.auto.tooling.bytebuddy.matcher.testclasses.B
import io.opentelemetry.auto.tooling.bytebuddy.matcher.testclasses.E
import io.opentelemetry.auto.tooling.bytebuddy.matcher.testclasses.F
import io.opentelemetry.auto.tooling.bytebuddy.matcher.testclasses.G
import io.opentelemetry.auto.util.test.AgentSpecification
import net.bytebuddy.description.type.TypeDescription

import static io.opentelemetry.auto.tooling.bytebuddy.matcher.AgentElementMatchers.hasInterface
import static net.bytebuddy.matcher.ElementMatchers.named

class SafeHasInterfaceMatcherTest extends AgentSpecification {

  def "test matcher #matcherClass.simpleName -> #type.simpleName"() {
    expect:
    hasInterface(matcher).matches(argument) == result

    where:
    matcherClass | type | result
    A            | A    | true
    A            | B    | true
    B            | A    | false
    A            | E    | true
    A            | F    | true
    F            | A    | false
    F            | F    | false
    F            | G    | false

    matcher = named(matcherClass.name)
    argument = TypeDescription.ForLoadedType.of(type)
  }

  def "test traversal exceptions"() {
    setup:
    def type = Mock(TypeDescription)
    def typeGeneric = Mock(TypeDescription.Generic)
    def matcher = hasInterface(named(Object.name))

    when:
    def result = matcher.matches(type)

    then:
    !result // default to false
    noExceptionThrown()
    1 * type.isInterface() >> true
    1 * type.asGenericType() >> typeGeneric
    1 * typeGeneric.asErasure() >> { throw new Exception("asErasure exception") }
    1 * typeGeneric.getTypeName() >> "typeGeneric-name"
    1 * type.getInterfaces() >> { throw new Exception("getInterfaces exception") }
    1 * type.getSuperClass() >> { throw new Exception("getSuperClass exception") }
    2 * type.getTypeName() >> "type-name"
    0 * _
  }
}