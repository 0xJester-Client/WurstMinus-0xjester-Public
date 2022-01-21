class testModule2(ModuleBase):

    def onUpdate(self):
        a = PythonUtils.getLoadedEntity()
        for x in a:
            ChatUtils.message('{ID}'.format(ID = x.func_145782_y()))

register(testModule2("MCTEST","Used to test shite."))
