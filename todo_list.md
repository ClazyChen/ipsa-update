排除小气泡的思路，会建立一个非常长的反馈电路，从最后一级流水一路反馈到第一级，引起32*32=1024级的门延迟，这可能会成为一个问题

> PAUSE need to pause FBUF (no PROCs)
>
> replace STGW to a primitive **SET** and the parameters represent which modules are to set (GW, PAR, MAT, EXE) (STXB is independent)
>
> update in each module are stored in different FIFOs

1. 在底层集中控制器中存储所有需要修改的配置

   - 上层通过PCIe DMA与集中控制器交互，发送配置信息（暂不考虑从数据平面发送信息到host）；
   - 需要详细设计在集中控制器中，各种配置信息如何存放，需要多少BRAM， size， length等；
   - PCIe与user logic地址映射；
   - 集中控制器与所有的TSP连接，应该将集中控制器放在所有TSP的中间位置以较少wire latency
2. 更新测试的use case

   - base design
   - 单更新（增、删、改）
   - 多更新（单功能由多stage组成）
3. 实验数据

   - 更新生效时间
   - 更新过程完成时间
   - 吞吐（测试方法
   - 不同use case下的数据包延迟（测试方法
   - 参考PANIC
