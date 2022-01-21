class testModule6(ModuleBase):
    
    testSetting = CheckboxSetting("test", False)
    testSetting1 = CheckboxSetting("test1", True)
    testSetting2 = CheckboxSetting("test2", False)
    testSetting3 = CheckboxSetting("test3", True)
    
    def __init__(self, name1, description1):
        self.name = name1
        self.description = description1
    
    def onUpdate(self):
        ChatUtils.message("Set: {var}".format(var = self.testSetting.isChecked()))    

    def onRender3D(self):
        PythonUtils.drawOutline(BlockPos(0,60,0), 50, 50, 100, 150, 2.0)

    def getSettings(self):
        settings = []
        settings.append(self.testSetting)
        settings.append(self.testSetting1)
        settings.append(self.testSetting2)
        settings.append(self.testSetting3)
        return settings

register(testModule6("TestRender1","Module that renders a box lol."))
