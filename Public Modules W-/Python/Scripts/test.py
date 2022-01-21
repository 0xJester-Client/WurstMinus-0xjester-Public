class testModule1(ModuleBase):
    def onUpdate(self):
        #health = Minecraft.getMinecraft().player.getHealth()
        #PythonUtils.clientChat("Your Health is {health1}".format(health1 = health))
        ChatUtils.message("Your mum gay lol")

register(testModule1("TestModule1", "Module used to testing."))
