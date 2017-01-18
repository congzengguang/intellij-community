/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.openapi.module

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.util.text.StringUtil
import java.util.*

/**
 * @author nik
 */
abstract class ModuleGrouper {
  abstract fun getGroupPath(module: Module): List<String>

  abstract fun getPresentableName(module: Module): String

  abstract fun getAllModules(): Array<Module>

  companion object {
    @JvmStatic
    @JvmOverloads
    fun instanceFor(project: Project, moduleModel: ModifiableModuleModel? = null): ModuleGrouper {
      val hasGroups = moduleModel?.hasModuleGroups() ?: ModuleManager.getInstance(project).hasModuleGroups()
      if (!isQualifiedModuleNamesEnabled() || hasGroups) {
        return ExplicitModuleGrouper(project, moduleModel)
      }
      return QualifiedNameGrouper(project, moduleModel)
    }
  }
}

fun isQualifiedModuleNamesEnabled() = Registry.`is`("project.qualified.module.names")

private abstract class ModuleGrouperBase(protected val project: Project, protected val model: ModifiableModuleModel?) : ModuleGrouper() {
  override fun getAllModules(): Array<Module> = model?.modules ?: ModuleManager.getInstance(project).modules
  protected fun getModuleName(module: Module) = model?.getNewName(module) ?: module.name
}

private class QualifiedNameGrouper(project: Project, model: ModifiableModuleModel?) : ModuleGrouperBase(project, model) {
  override fun getGroupPath(module: Module): List<String> {
    return getModuleName(module).split('.').dropLast(1)
  }

  override fun getPresentableName(module: Module) = StringUtil.getShortName(getModuleName(module))
}

private class ExplicitModuleGrouper(project: Project, model: ModifiableModuleModel?): ModuleGrouperBase(project, model) {
  override fun getGroupPath(module: Module): List<String> {
    val path = if (model != null) model.getModuleGroupPath(module) else ModuleManager.getInstance(project).getModuleGroupPath(module)
    return if (path != null) Arrays.asList(*path) else emptyList()
  }

  override fun getPresentableName(module: Module) = getModuleName(module)
}
