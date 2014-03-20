/* Copyright (C) 2014 SpringSource
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grails.datastore.gorm.boot.compiler

import grails.persistence.Entity
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.springframework.boot.cli.compiler.AstUtils;
import org.springframework.boot.cli.compiler.CompilerAutoConfiguration
import org.springframework.boot.cli.compiler.DependencyCustomizer;

/**
 * A compiler configuration that automatically adds the necessary imports
 *
 * @author Graeme Rocher
 * @since 1.0
 *
 */
@CompileStatic
class GormCompilerAutoConfiguration extends CompilerAutoConfiguration{
    @Override
    boolean matches(ClassNode classNode) {
        return AstUtils.hasAtLeastOneAnnotation(classNode, Entity.name, "Entity")
    }

    @Override
    void applyDependencies(DependencyCustomizer dependencies) throws CompilationFailedException {
        dependencies.ifAnyMissingClasses("grails.persistence.Entity")
                        .add("grails-datastore-gorm-hibernate4")
    }

    @Override
    void applyImports(ImportCustomizer imports) throws CompilationFailedException {
        imports.addStarImports("grails.persistence", "grails.gorm")
    }
}