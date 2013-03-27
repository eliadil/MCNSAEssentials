package com.mcnsa.essentials.components;

import com.mcnsa.essentials.annotations.ComponentInfo;
import com.mcnsa.essentials.annotations.DatabaseTableInfo;

@ComponentInfo(friendlyName = "Macros",
				description = "Easy way to group numerous commands into a single command",
				permsSettingsPrefix = "macros")
@DatabaseTableInfo(name = "macros",
					fields = { "name TINYTEXT", "permission TINYTEXT", "tempGroup TINYTEXT", "command TEXT" })
public class Macros {

}
