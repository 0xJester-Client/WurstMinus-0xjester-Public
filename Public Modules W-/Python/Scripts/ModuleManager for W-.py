#from me.third.right.utils.Interpreters import PythonUtils
print('Wurst- | Jython is present! | V1')

###
#
# Module Manager For Wurst- 2 Python Interpreter by 3rd#1703 ( Third_Right )
#
###

print(""" 
__        __              _          ____  
\ \      / /   _ _ __ ___| |_       |___ \ 
 \ \ /\ / / | | | '__/ __| __|____    __) |
  \ V  V /| |_| | |  \__ \ ||_____|  / __/ 
   \_/\_/  \__,_|_|  |___/\__|      |_____|
                                           
""")

class ModuleBase:
    enabled = False
    def __init__(self, name, description):
        self.name = name
        self.description = description

    #Runtime executions
    def onUpdate(self):
        pass
      
    #Toggle State
    def Toggle(self):
        if self.enabled == True:
            self.onDisabled()
            self.enabled = False
        else:
            self.onEnabled()
            self.enabled = True

    def setToggle(self, state):
        self.enabled = state
        if self.enabled == True:
            self.onDisabled()
            self.enabled = False
        else:
            self.onEnabled()
            self.enabled = True
            

    def onEnabled(self):
        pass

    def onDisabled(self):
        pass
    
    #Vars
    def getName(self):
        return self.name

    def getDescription(self):
        return self.description

    def isEnabled(self):
        return self.enabled

# Vars
moduleList = []

# Methods
def register(module):
    moduleList.append(module)

def runUpdate():
    for x in moduleList:
        if x.isEnabled() == True:
            x.onUpdate()

def getModuleList():
    modules = []
    for x in moduleList:
        modules.append("{name},{description},{toggleState}".format(name = x.getName(), description = x.getDescription(), toggleState = x.isEnabled()))
    return modules

def getModuleByName(name):
    for x in moduleList:
        if x.getName().lower() == name.lower():
            return x

print("Wurst- | Jython boot code finished.")

class testModule1(ModuleBase):
    def __init__(self):
        self.name = "TestModule1"
        self.description = "Module used to testing."

    def onUpdate(self):
        print("Test Module")

register(testModule1())

getModuleByName('TestModule1').Toggle()

runUpdate()

getModuleByName('TestModule1').Toggle()