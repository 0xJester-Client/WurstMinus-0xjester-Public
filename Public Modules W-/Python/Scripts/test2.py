class testModule5(ModuleBase):
    def onRender3D(self):
        PythonUtils.drawBoxFull(BlockPos(0,60,0), 255, 50, 100, 50)
        PythonUtils.drawBoxFull(BlockPos(0,61,0), 255, 50, 100, 50)

register(testModule5("TestRender","Module that renders a box lol."))
