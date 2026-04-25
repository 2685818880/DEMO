<template>
  <div class="chat-bi">
    <div style="display: flex; align-items: center; margin-bottom: 10px;">
      <h2 style="margin: 0;">ChatBI 智能查询</h2>
      <el-button type="text" icon="el-icon-question" style="margin-left: 8px; font-size: 18px;" @click="helpVisible = true"></el-button>
    </div>
    <p style="color: #909399; margin-bottom: 20px; font-size: 13px;">
      输入自然语言查询库存数据，例如："查A区滞销品"、"查物料档案"、"查物料类别"
    </p>

    <div class="chat-container" ref="chatContainer">
      <!-- 消息列表 -->
      <div class="chat-messages" ref="messagesContainer">
        <div
          v-for="(msg, idx) in messages"
          :key="idx"
          :class="['message-row', msg.role === 'user' ? 'user-row' : 'bot-row']"
        >
          <!-- 用户消息 -->
          <div v-if="msg.role === 'user'" class="user-message">
            <div class="message-bubble user-bubble">
              <i class="el-icon-user" style="margin-right: 6px;"></i>
              {{ msg.text }}
            </div>
          </div>

          <!-- Bot消息：加载中 -->
          <div v-else-if="msg.loading" class="bot-message">
            <div class="message-bubble bot-bubble">
              <i class="el-icon-loading"></i> 正在分析查询...
            </div>
          </div>

          <!-- Bot消息：错误 -->
          <div v-else-if="msg.text" class="bot-message">
            <div class="message-bubble bot-bubble error-bubble">
              <i class="el-icon-warning"></i> {{ msg.text }}
            </div>
          </div>

          <!-- Bot消息：结果卡片 -->
          <div v-else-if="msg.result" class="bot-message">
            <div class="result-card">
              <!-- 摘要 -->
              <el-alert
                :title="msg.result.summary"
                type="info"
                :closable="false"
                show-icon
                style="margin-bottom: 16px;"
              ></el-alert>

              <!-- 数据表格：慢动/临期/积压 -->
              <el-table
                v-if="msg.result.items && msg.result.items.length > 0 && msg.result.meta.parsedType !== 'INVENTORY_SUMMARY'"
                :data="msg.result.items"
                border
                stripe
                size="small"
                style="width: 100%; margin-bottom: 16px;"
                max-height="400"
              >
                <el-table-column prop="skuCode" label="SKU编码" min-width="120"></el-table-column>
                <el-table-column prop="skuName" label="SKU名称" min-width="140"></el-table-column>
                <el-table-column prop="batchNo" label="批次号" min-width="120"></el-table-column>
                <el-table-column prop="locationCode" label="库位" width="100"></el-table-column>
                <el-table-column prop="availableQty" label="可用数量" width="90"></el-table-column>
                <el-table-column prop="primaryUnit" label="单位" width="60"></el-table-column>
                <el-table-column prop="dwellDays" label="呆滞天数" width="90">
                  <template slot-scope="scope">
                    <el-tag :type="dwellTagType(scope.row.dwellDays)" size="mini">
                      {{ scope.row.dwellDays }}天
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="suggestion" label="建议" min-width="180"></el-table-column>
              </el-table>

              <!-- 数据表格：物料档案 -->
              <el-table
                v-if="msg.result.skuItems && msg.result.skuItems.length > 0"
                :data="msg.result.skuItems"
                border
                stripe
                size="small"
                style="width: 100%; margin-bottom: 16px;"
                max-height="400"
              >
                <el-table-column prop="skuCode" label="物料编码" min-width="120"></el-table-column>
                <el-table-column prop="skuName" label="物料名称" min-width="140"></el-table-column>
                <el-table-column prop="categoryCode" label="类别编码" min-width="100"></el-table-column>
                <el-table-column prop="categoryName" label="类别名称" min-width="100"></el-table-column>
                <el-table-column prop="barcode" label="条码" min-width="120"></el-table-column>
                <el-table-column prop="packageType" label="包装类型" width="90"></el-table-column>
                <el-table-column label="保质期" width="90">
                  <template slot-scope="scope">
                    {{ scope.row.durationOfValidity ? scope.row.durationOfValidity + scope.row.validityDateUnit : '-' }}
                  </template>
                </el-table-column>
                <el-table-column prop="skuBatchFlag" label="批次管理" width="80">
                  <template slot-scope="scope">
                    <el-tag :type="scope.row.skuBatchFlag ? 'success' : 'info'" size="mini">{{ scope.row.skuBatchFlag ? '是' : '否' }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="blockState" label="冻结" width="60">
                  <template slot-scope="scope">
                    <el-tag :type="scope.row.blockState ? 'danger' : 'success'" size="mini">{{ scope.row.blockState ? '是' : '否' }}</el-tag>
                  </template>
                </el-table-column>
              </el-table>

              <!-- 数据表格：物料类别 -->
              <el-table
                v-if="msg.result.skuCategoryItems && msg.result.skuCategoryItems.length > 0"
                :data="msg.result.skuCategoryItems"
                border
                stripe
                size="small"
                style="width: 100%; margin-bottom: 16px;"
                max-height="400"
              >
                <el-table-column prop="categoryCode" label="类别编码" min-width="120"></el-table-column>
                <el-table-column prop="categoryName" label="类别名称" min-width="140"></el-table-column>
                <el-table-column prop="categoryDesc" label="描述" min-width="160"></el-table-column>
                <el-table-column prop="parentCategoryName" label="父级类别" min-width="100"></el-table-column>
                <el-table-column prop="treeLevel" label="层级" width="60"></el-table-column>
                <el-table-column prop="childCount" label="子类数量" width="80"></el-table-column>
              </el-table>

              <!-- 数据表格：库位 -->
              <el-table
                v-if="msg.result.locationItems && msg.result.locationItems.length > 0"
                :data="msg.result.locationItems"
                border
                stripe
                size="small"
                style="width: 100%; margin-bottom: 16px;"
                max-height="400"
              >
                <el-table-column prop="locNo" label="库位号" min-width="120"></el-table-column>
                <el-table-column prop="houseCode" label="库号" width="80"></el-table-column>
                <el-table-column label="坐标" width="120">
                  <template slot-scope="scope">{{ scope.row.xPos }}-{{ scope.row.yPos }}-{{ scope.row.zPos }}</template>
                </el-table-column>
                <el-table-column prop="locType" label="库位类型" width="90"></el-table-column>
                <el-table-column prop="storageStatus" label="库存状态" width="90"></el-table-column>
                <el-table-column prop="locUseStatus" label="使用状态" width="80"></el-table-column>
                <el-table-column prop="forbidIn" label="入锁" width="60">
                  <template slot-scope="scope"><el-tag :type="scope.row.forbidIn ? 'danger' : 'success'" size="mini">{{ scope.row.forbidIn ? '是' : '否' }}</el-tag></template>
                </el-table-column>
                <el-table-column prop="forbidOut" label="出锁" width="60">
                  <template slot-scope="scope"><el-tag :type="scope.row.forbidOut ? 'danger' : 'success'" size="mini">{{ scope.row.forbidOut ? '是' : '否' }}</el-tag></template>
                </el-table-column>
                <el-table-column prop="locIsError" label="异常" width="60">
                  <template slot-scope="scope"><el-tag :type="scope.row.locIsError ? 'danger' : 'success'" size="mini">{{ scope.row.locIsError ? '是' : '否' }}</el-tag></template>
                </el-table-column>
              </el-table>

              <!-- 数据表格：容器 -->
              <el-table
                v-if="msg.result.containerItems && msg.result.containerItems.length > 0"
                :data="msg.result.containerItems"
                border
                stripe
                size="small"
                style="width: 100%; margin-bottom: 16px;"
                max-height="400"
              >
                <el-table-column prop="containerCode" label="容器编码" min-width="130"></el-table-column>
                <el-table-column prop="containerTypeCode" label="类型编码" width="100"></el-table-column>
                <el-table-column prop="containerTypeName" label="类型名称" width="100"></el-table-column>
                <el-table-column label="尺寸(长x宽x高)" width="150">
                  <template slot-scope="scope">{{ scope.row.containerLength || '-' }}x{{ scope.row.containerWidth || '-' }}x{{ scope.row.containerHeight || '-' }}</template>
                </el-table-column>
                <el-table-column prop="containerWeight" label="重量" width="70"></el-table-column>
                <el-table-column prop="usageCount" label="使用次数" width="80"></el-table-column>
              </el-table>

              <!-- 数据表格：定时器 -->
              <el-table
                v-if="msg.result.schedulerItems && msg.result.schedulerItems.length > 0"
                :data="msg.result.schedulerItems"
                border
                stripe
                size="small"
                style="width: 100%; margin-bottom: 16px;"
                max-height="400"
              >
                <el-table-column prop="jobCode" label="任务编码" min-width="100"></el-table-column>
                <el-table-column prop="triggerCode" label="触发器编码" min-width="110"></el-table-column>
                <el-table-column prop="jobType" label="任务类型" width="150"></el-table-column>
                <el-table-column prop="triggerCorn" label="Cron表达式" width="150"></el-table-column>
                <el-table-column prop="description" label="描述" min-width="130"></el-table-column>
                <el-table-column prop="nextTriggerDatetime" label="下次触发时间" min-width="140"></el-table-column>
                <el-table-column prop="pause" label="暂停" width="60">
                  <template slot-scope="scope"><el-tag :type="scope.row.pause ? 'warning' : 'success'" size="mini">{{ scope.row.pause ? '是' : '否' }}</el-tag></template>
                </el-table-column>
                <el-table-column prop="finished" label="完成" width="60">
                  <template slot-scope="scope"><el-tag :type="scope.row.finished ? 'success' : 'info'" size="mini">{{ scope.row.finished ? '是' : '否' }}</el-tag></template>
                </el-table-column>
                <el-table-column prop="disable" label="禁用" width="60">
                  <template slot-scope="scope"><el-tag :type="scope.row.disable ? 'danger' : 'success'" size="mini">{{ scope.row.disable ? '是' : '否' }}</el-tag></template>
                </el-table-column>
              </el-table>

              <!-- 数据表格：任务调度 -->
              <el-table
                v-if="msg.result.dispatchInfoItems && msg.result.dispatchInfoItems.length > 0"
                :data="msg.result.dispatchInfoItems"
                border
                stripe
                size="small"
                style="width: 100%; margin-bottom: 16px;"
                max-height="400"
              >
                <el-table-column prop="dispatchCode" label="调度编码" min-width="120"></el-table-column>
                <el-table-column prop="dispatchName" label="调度名称" min-width="120"></el-table-column>
                <el-table-column prop="dispatchType" label="调度类型" width="90"></el-table-column>
                <el-table-column prop="dispatchStatus" label="状态" width="80">
                  <template slot-scope="scope"><el-tag :type="scope.row.dispatchStatus === 'FINISHED' ? 'success' : scope.row.dispatchStatus === 'ERROR' ? 'danger' : 'warning'" size="mini">{{ scope.row.dispatchStatus }}</el-tag></template>
                </el-table-column>
                <el-table-column prop="houseCode" label="仓库" width="70"></el-table-column>
                <el-table-column prop="containerCode" label="容器" width="100"></el-table-column>
                <el-table-column prop="businessFormNo" label="业务单号" min-width="130"></el-table-column>
                <el-table-column prop="createDatetime" label="创建时间" min-width="140"></el-table-column>
              </el-table>

              <!-- 数据表格：任务信息 -->
              <el-table
                v-if="msg.result.dispatchJobItems && msg.result.dispatchJobItems.length > 0"
                :data="msg.result.dispatchJobItems"
                border
                stripe
                size="small"
                style="width: 100%; margin-bottom: 16px;"
                max-height="400"
              >
                <el-table-column prop="taskNo" label="任务号" min-width="130"></el-table-column>
                <el-table-column prop="taskType" label="任务类型" width="90"></el-table-column>
                <el-table-column prop="taskStatus" label="状态" width="80">
                  <template slot-scope="scope"><el-tag :type="scope.row.taskStatus === 'FINISHED' ? 'success' : scope.row.taskStatus === 'ERROR' ? 'danger' : 'warning'" size="mini">{{ scope.row.taskStatus }}</el-tag></template>
                </el-table-column>
                <el-table-column prop="fromPos" label="来源" width="100"></el-table-column>
                <el-table-column prop="toPos" label="目标" width="100"></el-table-column>
                <el-table-column prop="houseCode" label="仓库" width="60"></el-table-column>
                <el-table-column prop="containerCode" label="容器" width="90"></el-table-column>
                <el-table-column prop="taskLevel" label="等级" width="60"></el-table-column>
                <el-table-column prop="errorCode" label="异常编码" width="90"></el-table-column>
                <el-table-column prop="sendTime" label="下发时间" min-width="130"></el-table-column>
              </el-table>

              <!-- 数据表格：库存汇总 -->
              <el-table
                v-if="msg.result.summaryItems && msg.result.summaryItems.length > 0"
                :data="msg.result.summaryItems"
                border
                stripe
                size="small"
                style="width: 100%; margin-bottom: 16px;"
                max-height="400"
              >
                <el-table-column prop="skuCode" label="SKU编码" min-width="120"></el-table-column>
                <el-table-column prop="skuName" label="SKU名称" min-width="140"></el-table-column>
                <el-table-column prop="totalAvailableQty" label="可用数量" width="100"></el-table-column>
                <el-table-column prop="totalPrimaryQty" label="总数量" width="90"></el-table-column>
                <el-table-column prop="batchCount" label="批次数" width="80"></el-table-column>
                <el-table-column prop="locationCount" label="库位数" width="80"></el-table-column>
                <el-table-column prop="primaryUnit" label="单位" width="60"></el-table-column>
              </el-table>

              <!-- 建议列表 -->
              <div
                v-if="msg.result.suggestions && msg.result.suggestions.length > 0"
                style="margin-bottom: 16px;"
              >
                <h4 style="margin: 0 0 8px 0; font-size: 14px; color: #606266;">
                  <i class="el-icon-document"></i> 处理建议
                </h4>
                <ul style="margin: 0; padding-left: 20px;">
                  <li
                    v-for="(s, si) in msg.result.suggestions"
                    :key="si"
                    style="font-size: 13px; color: #606266; line-height: 1.8;"
                  >{{ s }}</li>
                </ul>
              </div>

              <!-- 操作按钮 -->
              <div
                v-if="msg.result.actionButtons && msg.result.actionButtons.length > 0"
              >
                <h4 style="margin: 0 0 8px 0; font-size: 14px; color: #606266;">
                  <i class="el-icon-setting"></i> 操作
                </h4>
                <el-button
                  v-for="(btn, bi) in msg.result.actionButtons"
                  :key="bi"
                  :type="btn.type"
                  :icon="btn.icon"
                  size="small"
                  style="margin-right: 10px;"
                  @click="handleAction(btn, msg)"
                >{{ btn.label }}</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="2"
          placeholder="输入查询语句，例如：查A区滞销品、查物料档案、查物料类别"
          @keydown.enter.native.prevent="handleSend"
        ></el-input>
        <el-button
          type="primary"
          :loading="loading"
          :disabled="!inputText.trim()"
          @click="handleSend"
          style="margin-left: 10px; flex-shrink: 0;"
        >
          {{ loading ? '查询中...' : '发送' }}
        </el-button>
      </div>
    </div>

    <!-- 帮助对话框 -->
    <el-dialog :visible.sync="helpVisible" title="ChatBI 使用帮助" width="700px" :close-on-click-modal="true">
      <div class="help-content">
        <h3>📌 支持的功能</h3>

        <h4>1. 库存汇总</h4>
        <p>查询整个仓库或指定区域的库存概况，按SKU汇总可用数量、批次数、库位数。</p>
        <p><b>关键词：</b><el-tag size="mini">汇总</el-tag> <el-tag size="mini">按产品</el-tag> <el-tag size="mini">按物料</el-tag> <el-tag size="mini">库存分布</el-tag></p>
        <p><b>示例：</b><code>查库存汇总</code>、<code>查A区库存分布</code></p>
        <hr>

        <h4>2. 滞销品查询</h4>
        <p>查找长时间未动销的库存物料，默认超过90天未更新视为滞销。</p>
        <p><b>关键词：</b><el-tag size="mini">滞销</el-tag> <el-tag size="mini">呆滞</el-tag> <el-tag size="mini">慢动</el-tag></p>
        <p><b>示例：</b><code>查滞销品</code>、<code>查A区滞销品</code>、<code>查超过60天未动的呆滞品</code></p>
        <p><b>说明：</b>可在查询中指定天数，如"60天"、"180天"</p>
        <hr>

        <h4>3. 临期品预警</h4>
        <p>查找即将过期的物料，默认提前30天预警。</p>
        <p><b>关键词：</b><el-tag size="mini">过期</el-tag> <el-tag size="mini">临期</el-tag> <el-tag size="mini">效期</el-tag></p>
        <p><b>示例：</b><code>查临期品</code>、<code>查B区过期物料</code></p>
        <hr>

        <h4>4. 积压品分析</h4>
        <p>查找库存数量过大的物料，默认单SKU可用量 ≥ 1000 视为积压。</p>
        <p><b>关键词：</b><el-tag size="mini">积压</el-tag> <el-tag size="mini">过剩</el-tag> <el-tag size="mini">过多</el-tag></p>
        <p><b>示例：</b><code>查积压品</code>、<code>查A区库存过多的物料</code></p>
        <hr>

        <h4>5. 物料档案查询</h4>
        <p>查询物料档案信息（wms_sku），可按编码、名称或条码模糊搜索。</p>
        <p><b>关键词：</b><el-tag size="mini">物料档案</el-tag> <el-tag size="mini">物料信息</el-tag> <el-tag size="mini">产品档案</el-tag></p>
        <p><b>示例：</b><code>查物料档案</code>、<code>查物料档案 电子</code></p>
        <p><b>说明：</b>可在查询后面附加关键词进行搜索</p>
        <hr>

        <h4>6. 物料类别查询</h4>
        <p>查询物料类别信息（wms_sku_category），展示类别层级结构。</p>
        <p><b>关键词：</b><el-tag size="mini">物料类别</el-tag> <el-tag size="mini">物料分类</el-tag> <el-tag size="mini">产品类别</el-tag> <el-tag size="mini">产品分类</el-tag></p>
        <p><b>示例：</b><code>查物料类别</code>、<code>查物料分类 原材料</code></p>
        <hr>

        <h4>7. 库位查询</h4>
        <p>查询库位信息（wms_storage_location），展示库位号、坐标、类型、状态、锁定标记。</p>
        <p><b>关键词：</b><el-tag size="mini">库位</el-tag> <el-tag size="mini">货位</el-tag></p>
        <p><b>示例：</b><code>查库位</code>、<code>查库位 A-01</code></p>
        <hr>

        <h4>8. 容器查询</h4>
        <p>查询容器/托盘信息（wms_container），展示容器编码、类型、尺寸、使用次数。</p>
        <p><b>关键词：</b><el-tag size="mini">容器</el-tag> <el-tag size="mini">托盘</el-tag></p>
        <p><b>示例：</b><code>查容器</code>、<code>查托盘 TRAY001</code></p>
        <hr>

        <h4>9. 定时器管理查询</h4>
        <p>查询定时器/调度器配置（sys_scheduler_manage），展示任务编码、Cron表达式、状态、下次触发时间。</p>
        <p><b>关键词：</b><el-tag size="mini">定时器</el-tag> <el-tag size="mini">调度器</el-tag> <el-tag size="mini">定时任务</el-tag> <el-tag size="mini">计划任务</el-tag></p>
        <p><b>示例：</b><code>查定时器</code>、<code>查定时任务</code></p>
        <hr>

        <h4>10. 任务调度查询</h4>
        <p>查询任务调度记录（wms_dispatch_info），展示调度编码、类型、状态、关联单据。</p>
        <p><b>关键词：</b><el-tag size="mini">任务调度</el-tag> <el-tag size="mini">调度信息</el-tag></p>
        <p><b>示例：</b><code>查任务调度</code>、<code>查调度信息</code></p>
        <hr>

        <h4>11. 任务信息查询</h4>
        <p>查询作业任务记录（wms_dispatch_job），展示任务号、类型、状态、来源/目标地址。</p>
        <p><b>关键词：</b><el-tag size="mini">任务信息</el-tag> <el-tag size="mini">作业信息</el-tag> <el-tag size="mini">作业任务</el-tag></p>
        <p><b>示例：</b><code>查任务信息</code>、<code>查作业信息</code></p>
        <hr>

        <h4>12. 区域过滤</h4>
        <p>在查询中加入区域名称可限定范围，格式为 <code>X区</code>。</p>
        <p><b>示例：</b><code>A区</code>、<code>B区</code>、<code>1区</code></p>
        <p><b>示例查询：</b><code>查A区滞销品并生成处理建议</code></p>
        <hr>

        <h3>📌 12. 出库策略说明：单工双深出库策略V2.0</h3>
        <p>适用于单工位双深度（Double-Deep）货架系统的出库库位分配策略。双深度货架中，<b>浅库位（Single）</b>在前排可直接取货，<b>深库位（Double）</b>在后排需先移开浅库位货物才能取货。深浅库位通过 <code>location_mapping</code> 关联。</p>

        <h4>四种分配类型</h4>
        <table class="help-table">
          <tr><th>类型</th><th>说明</th><th>场景</th></tr>
          <tr><td><code>SINGLE</code></td><td>分配无深库位关联的浅库位</td><td>深库位不可用时，分配独立浅库位（兜底）</td></tr>
          <tr><td><code>DOUBLE</code></td><td>分配无关联的深库位</td><td>仅深库位有货，直接出深库位</td></tr>
          <tr><td><code>SINGLE_RELATION</code></td><td>分配浅库位（优先浅），不足时回退深库位</td><td>深浅都有货，优先从浅库位出库</td></tr>
          <tr><td><code>DOUBLE_RELATION</code></td><td>分配浅库位不可用时的关联深库位</td><td>浅库位无货/被锁定/出库中，回退到对应的深库位</td></tr>
        </table>

        <h4>三阶段执行链</h4>
        <p>按以下优先级依次执行，任一阶段满足需求数量则跳过后续阶段：</p>
        <ol style="padding-left: 20px; margin: 8px 0;">
          <li><b>策略1 → DOUBLE_RELATION：</b>浅库位不可用时（浅库位空/被锁定/批次不一致/有出库任务），回退到关联的深库位出库</li>
          <li><b>策略2 → SINGLE_RELATION：</b>深浅库位都满足条件时，优先从浅库位出库；浅库位数量不足时，继续从关联深库位出库</li>
          <li><b>策略3 → SINGLE：</b>深库位都不可用时，分配无关联的浅库位（最终兜底）</li>
        </ol>

        <h4>浅库位"不可用"判定条件</h4>
        <p>以下任一条件成立，即判定浅库位不可用，触发回退深库位：</p>
        <ul class="help-limits">
          <li>浅库位<b>无货</b>且<b>未被预约</b></li>
          <li>浅库位<b>有出库任务</b>正在执行（Waiting/Executing）</li>
          <li>浅库位有货但<b>批次不一致</b>（与待出库SKU+批次不匹配）</li>
          <li>浅库位有货、批次一致但 <b>remark 字段不为空</b></li>
        </ul>

        <h4>业务约束</h4>
        <ul class="help-limits">
          <li>只操作 <code>loc_type = 'cubic'</code> 的立体库位</li>
          <li>只操作 <code>storage_status = 'haveGoods'</code> 的有货库位</li>
          <li>跳过 <code>forbid_out = 1</code>（禁止出库）和 <code>loc_is_error = 1</code>（异常库位）的库位</li>
          <li>过滤 <code>batch_status = 'S'</code> 的冻结批次物料</li>
          <li>过滤 <code>available_qty ≤ 0</code> 的零可用量物料</li>
          <li>过滤 <code>remark IS NOT NULL</code> 的物料（有标记的物料不可分配）</li>
          <li>只选择巷道启用（<code>roadway_enable = '1'</code>）的库位</li>
          <li>物料需在有效期内（<code>expire_date</code> 和 <code>availability_date</code> 校验）</li>
        </ul>

        <h4>分配结果逻辑</h4>
        <ul class="help-limits">
          <li>对每个候选库位，按 <code>availability_date ASC</code> 排序（先进先出）</li>
          <li>根据需求数量决定 <b>全部锁定</b>（LockAllType.Y）或 <b>部分锁定</b>（LockAllType.N）</li>
          <li>锁定的物料会写入 <code>storage_locked_id</code>，防止被其他任务重复分配</li>
          <li>加锁失败（已被其他任务锁定）会自动跳过，继续下一个候选库位</li>
          <li>当浅库位满足需求后，对应的深库位会被标记 <code>remark = 'next'</code>，下次优先分配</li>
        </ul>

        <p><b>策略编码：</b><code>DepthBindAllotOutPolicy</code> &nbsp;|&nbsp; <b>实现类：</b><code>DepthBindDeepOutStorageAllotServiceImpl</code></p>
        <hr>

        <h3>📌 13. 入库策略说明：单工双深入库策略V2.0</h3>
        <p>适用于单工位双深度（Double-Deep）货架系统的<b>入库库位分配</b>策略。当货物到库需要上架时，系统根据托盘是否有货（实托/空托）、SKU、批次等信息，自动分配最合适的库位。深浅库位通过 <code>location_mapping</code> 关联。</p>
        <p><b>策略名称：</b><code>单工双深入库策略(同批、同类存放)V2.0</code></p>

        <h4>三种分配类型（按优先级依次执行）</h4>
        <table class="help-table">
          <tr><th>优先级</th><th>类型</th><th>说明</th><th>场景</th></tr>
          <tr><td>1</td><td><code>SINGLE_RELATION</code></td><td>深库位有货同物料同批次时，分配到绑定的浅库位</td><td>深库位存放了同SKU同批次的货，新到货物优先放到关联的浅库位，便于后续出库</td></tr>
          <tr><td>2</td><td><code>DOUBLE_RELATION</code></td><td>浅库位空时，分配到绑定的深库位</td><td>浅库位是空的、未被分配，则新货物放到对应的深库位</td></tr>
          <tr><td>3</td><td><code>SINGLE</code></td><td>分配无关联的普通浅库位（兜底）</td><td>以上都不满足时，分配到任意空的独立浅库位</td></tr>
        </table>

        <h4>实托 vs 空托入库</h4>
        <ul class="help-limits">
          <li><b>实托入库（有SKU）：</b>筛选同SKU的深库位关联浅库位；启用 <code>sameBatchStorage</code> 时额外匹配同批次</li>
          <li><b>空托入库（无SKU）：</b>筛选有空托盘的深库位关联浅库位，不涉及SKU/批次筛选</li>
        </ul>

        <h4>库位筛选条件</h4>
        <ul class="help-limits">
          <li>只操作 <code>loc_type = 'cubic'</code> 的立体库位</li>
          <li>只操作 <code>storage_status = 'empty'</code> 的空库位</li>
          <li>跳过 <code>forbid_in = 1</code>（禁止入库）的库位</li>
          <li>跳过 <code>loc_is_error = 1</code>（异常库位）的库位</li>
          <li>跳过已分配未执行 <code>is_location_alloted = 1</code> 的库位</li>
          <li>跳过已有库存记录（<code>wms_storage_location_inventory</code>）的库位</li>
          <li>只选择巷道启用（<code>roadway_enable = 1</code>）的巷道</li>
        </ul>

        <h4>规格匹配（可选）</h4>
        <p>如果策略配置了高度匹配参数，还会按容器尺寸匹配合适的库位规格：</p>
        <ul class="help-limits">
          <li>库位高度 ≥ 容器高度（<code>loc_height >= containerHeight</code>）</li>
          <li>库位宽度 ≥ 容器宽度（<code>loc_width >= containerWidth</code>）</li>
          <li>库位深度 ≥ 容器长度（<code>loc_length >= containerLength</code>）</li>
          <li>库位承重 ≥ 容器重量（<code>loc_allow_weight >= containerWeight</code>）</li>
        </ul>

        <h4>排序规则</h4>
        <ul class="help-limits">
          <li>无关联深库位的浅库位优先（没有 <code>location_mapping</code> 的浅库位排在前面）</li>
          <li>按库位规格从小到大排序（高度、宽度、深度、承重升序）</li>
        </ul>

        <h4>巷道均分策略</h4>
        <p>当系统配置 <code>AverageConfig = true</code> 且未指定巷道时，会在各个巷道之间均分入库任务，避免单一巷道负载过高。</p>

        <p><b>策略编码：</b><code>DepthBindDeepAllotInPolicy</code> &nbsp;|&nbsp; <b>实现类：</b><code>DepthBindDeepInLocationAllotServiceImpl</code></p>
        <hr>

        <h3>📌 支持的操作（查询结果后的按钮）</h3>
        <table class="help-table">
          <tr><th>操作</th><th>功能</th><th>生成单据</th></tr>
          <tr><td><el-tag type="primary" size="mini">生成移库单</el-tag></td><td>对查询结果中的物料创建移库单</td><td>wms_transfer</td></tr>
          <tr><td><el-tag type="warning" size="mini">生成盘点单</el-tag></td><td>对查询结果中的物料创建盘点单</td><td>wms_take_stock</td></tr>
          <tr><td><el-tag type="success" size="mini">生成收货单</el-tag></td><td>对查询结果中的物料创建ASN收货单</td><td>wms_asn</td></tr>
          <tr><td><el-tag type="danger" size="mini">生成发货单</el-tag></td><td>对查询结果中的物料创建发货单</td><td>wms_requisition_order</td></tr>
          <tr><td><el-tag type="info" size="mini">生成组盘单</el-tag></td><td>对查询结果中的物料创建组盘单</td><td>wms_palletize_form</td></tr>
        </table>
        <p style="font-size: 12px; color: #909399;">* 执行操作前需要选择目标仓库</p>
        <p style="font-size: 12px; color: #909399;">* 操作按钮仅出现在库存相关查询结果中（滞销/临期/积压/汇总），基础数据查询不含操作按钮</p>
        <hr>

        <h3>⚠️ 边界与限制</h3>
        <ul class="help-limits">
          <li><b>区域识别：</b>仅支持单字符区域名，如 <code>A区</code>、<code>B区</code>；<code>华东区</code> 只会识别为 <code>东区</code></li>
          <li><b>临期预警：</b>预警天数固定为30天，不支持在查询中自定义</li>
          <li><b>积压阈值：</b>固定为可用量 ≥ 1000，不支持在查询中自定义</li>
          <li><b>滞销天数：</b>默认90天，可在查询中指定，如"查超过60天未动的滞销品"</li>
          <li><b>数据范围：</b>库存分析查 wms_storage_material；物料档案查 wms_sku；物料类别查 wms_sku_category；库位查 wms_storage_location；容器查 wms_container；定时器查 sys_scheduler_manage；任务调度查 wms_dispatch_info；任务信息查 wms_dispatch_job</li>
          <li><b>区域映射：</b>区域过滤依赖 wms_relate_zone_and_location 表，若未配置区域-货位映射，区域过滤不生效</li>
          <li><b>出库策略：</b>单工双深出库策略（DepthBindDeepOutStorageAllotServiceImpl）支持"查单工双深出库策略"等查询</li>
          <li><b>入库策略：</b>单工双深入库策略（DepthBindDeepInLocationAllotServiceImpl）支持同批同类存放、巷道均分、规格匹配</li>
          <li><b>物料搜索：</b>物料档案和类别查询支持模糊搜索，在查询语句后附加关键词即可，如"查物料档案 电子"</li>
          <li><b>创建单据：</b>操作按钮直接写入数据库，绕过标准业务审批流程，生成的单据处于初始状态</li>
          <li><b>不支持：</b>暂不支持模糊语义理解、多条件组合查询、自定义报表生成</li>
        </ul>
      </div>
    </el-dialog>

    <!-- 仓库选择对话框 -->
    <el-dialog
      :visible.sync="warehouseDialog.visible"
      title="选择仓库"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form label-width="80px">
        <el-form-item label="仓库">
          <el-select v-model="warehouseDialog.selectedHouseCode" placeholder="请选择仓库" style="width: 100%">
            <el-option
              v-for="w in warehouseDialog.warehouses"
              :key="w.house_code"
              :label="w.warehouse_name || w.house_code"
              :value="w.house_code"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="warehouseDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="warehouseDialog.loading" @click="confirmAction">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { chatBiQuery, createTransferFromChatBI, createStocktakeFromChatBI, createAsnFromChatBI, createRequisitionFromChatBI, createPalletizeFromChatBI, getWarehouses } from '@/api/ai'

export default {
  name: 'ChatBI',
  data() {
    return {
      messages: [],
      inputText: '',
      loading: false,
      helpVisible: false,
      warehouseDialog: {
        visible: false,
        warehouses: [],
        selectedHouseCode: '',
        loading: false,
        pendingAction: null,
        pendingMsg: null
      }
    }
  },
  methods: {
    async handleSend() {
      const text = this.inputText.trim()
      if (!text || this.loading) return

      // 添加用户消息
      this.messages.push({ role: 'user', text })
      this.inputText = ''

      // 添加加载中占位
      const botMsg = { role: 'bot', text: '', result: null, loading: true }
      this.messages.push(botMsg)
      this.loading = true

      this.$nextTick(() => this.scrollToBottom())

      try {
        const res = await chatBiQuery({ query: text })
        if (res.success && res.data) {
          botMsg.result = res.data
          botMsg.loading = false
        } else {
          botMsg.text = res.message || '查询失败，请重试'
          botMsg.loading = false
        }
      } catch (e) {
        botMsg.text = '请求失败: ' + (e.message || '网络错误')
        botMsg.loading = false
      } finally {
        this.loading = false
        this.$nextTick(() => this.scrollToBottom())
      }
    },

    scrollToBottom() {
      const container = this.$refs.messagesContainer
      if (container) {
        container.scrollTop = container.scrollHeight
      }
    },

    dwellTagType(days) {
      if (days >= 180) return 'danger'
      if (days >= 90) return 'warning'
      if (days >= 60) return 'info'
      return 'success'
    },

    async handleAction(btn, msg) {
      // 显示仓库选择对话框
      const dialog = this.warehouseDialog
      dialog.visible = true
      dialog.loading = true
      dialog.selectedHouseCode = ''
      dialog.pendingAction = btn.action
      dialog.pendingMsg = msg

      try {
        const res = await getWarehouses()
        if (res.success && res.data) {
          dialog.warehouses = res.data.filter(function(w) { return w.house_code })
          if (dialog.warehouses.length === 1) {
            dialog.selectedHouseCode = dialog.warehouses[0].house_code
          } else if (dialog.warehouses.length > 0) {
            dialog.selectedHouseCode = dialog.warehouses[0].house_code
          }
        }
      } catch (e) {
        this.$message.error('加载仓库列表失败')
      } finally {
        dialog.loading = false
      }
    },

    async confirmAction() {
      const dialog = this.warehouseDialog
      if (!dialog.selectedHouseCode) {
        this.$message.warning('请选择仓库')
        return
      }

      const btnAction = dialog.pendingAction
      const msg = dialog.pendingMsg
      dialog.visible = false

      // 从消息结果中提取物料列表
      var items = []
      if (msg.result.items && msg.result.items.length > 0) {
        items = msg.result.items.map(function(item) {
          return {
            skuCode: item.skuCode,
            skuName: item.skuName,
            batchNo: item.batchNo || '',
            locationCode: item.locationCode || '',
            containerCode: item.containerCode || '',
            availableQty: item.availableQty || item.primaryQty || 0,
            primaryUnit: item.primaryUnit || ''
          }
        })
      } else if (msg.result.summaryItems && msg.result.summaryItems.length > 0) {
        items = msg.result.summaryItems.map(function(item) {
          return {
            skuCode: item.skuCode,
            skuName: item.skuName,
            batchNo: '',
            locationCode: '',
            containerCode: '',
            availableQty: item.totalAvailableQty || 0,
            primaryUnit: item.primaryUnit || ''
          }
        })
      }

      if (items.length === 0) {
        this.$message.warning('没有可操作的物料数据')
        return
      }

      const requestData = {
        houseCode: dialog.selectedHouseCode,
        zoneName: msg.result.meta ? msg.result.meta.parsedZone : '',
        items: items
      }

      try {
        var res
        if (btnAction === 'CREATE_TRANSFER_ORDER') {
          res = await createTransferFromChatBI(requestData)
        } else if (btnAction === 'CREATE_STOCKTAKE_ORDER') {
          res = await createStocktakeFromChatBI(requestData)
        } else if (btnAction === 'CREATE_ASN_ORDER') {
          res = await createAsnFromChatBI(requestData)
        } else if (btnAction === 'CREATE_REQUISITION_ORDER') {
          res = await createRequisitionFromChatBI(requestData)
        } else if (btnAction === 'CREATE_PALLETIZE_ORDER') {
          res = await createPalletizeFromChatBI(requestData)
        } else {
          this.$message.warning('未知操作: ' + btnAction)
          return
        }

        if (res.success && res.data) {
          const appPath = '/app/wms-platform/view'
          if (btnAction === 'CREATE_TRANSFER_ORDER') {
            this.$message.success('移库单 ' + res.data.transferNo + ' 创建成功，共 ' + res.data.itemCount + ' 项物料')
            window.open(appPath + '/transfer', '_blank')
          } else if (btnAction === 'CREATE_STOCKTAKE_ORDER') {
            this.$message.success('盘点单 ' + res.data.stockFormNo + ' 创建成功，共 ' + res.data.itemCount + ' 项物料')
            window.open(appPath + '/take-stock', '_blank')
          } else if (btnAction === 'CREATE_ASN_ORDER') {
            this.$message.success('收货单 ' + res.data.formNo + ' 创建成功，共 ' + res.data.itemCount + ' 项物料')
            window.open(appPath + '/asn', '_blank')
          } else if (btnAction === 'CREATE_REQUISITION_ORDER') {
            this.$message.success('发货单 ' + res.data.formNo + ' 创建成功，共 ' + res.data.itemCount + ' 项物料')
            window.open(appPath + '/outbound', '_blank')
          } else if (btnAction === 'CREATE_PALLETIZE_ORDER') {
            this.$message.success('组盘单 ' + res.data.formNo + ' 创建成功，共 ' + res.data.itemCount + ' 项物料')
            window.open(appPath + '/palletize', '_blank')
          }
        } else {
          this.$message.error(res.message || '操作失败')
        }
      } catch (e) {
        this.$message.error('操作失败: ' + (e.message || '未知错误'))
      }
    }
  }
}
</script>

<style scoped>
.chat-bi {
  padding: 20px;
  height: calc(100vh - 160px);
  display: flex;
  flex-direction: column;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #f5f7fa;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message-row {
  margin-bottom: 16px;
}

.user-row {
  display: flex;
  justify-content: flex-end;
}

.bot-row {
  display: flex;
  justify-content: flex-start;
}

.message-bubble {
  max-width: 80%;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
}

.user-bubble {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 2px;
}

.bot-bubble {
  background: #fff;
  color: #303133;
  border: 1px solid #e4e7ed;
  border-bottom-left-radius: 2px;
}

.error-bubble {
  color: #f56c6c;
  border-color: #fbc4c4;
  background: #fef0f0;
}

.result-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  max-width: 95%;
}

.chat-input-area {
  display: flex;
  align-items: flex-start;
  padding: 12px 16px;
  border-top: 1px solid #e4e7ed;
  background: #fff;
}

.chat-input-area .el-input {
  flex: 1;
}

.help-content {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  max-height: 60vh;
  overflow-y: auto;
}
.help-content h3 {
  font-size: 16px;
  margin: 16px 0 8px;
  color: #303133;
}
.help-content h4 {
  font-size: 14px;
  margin: 12px 0 4px;
  color: #606266;
}
.help-content hr {
  border: none;
  border-top: 1px solid #ebeef5;
  margin: 12px 0;
}
.help-content code {
  background: #f5f7fa;
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 13px;
  color: #409eff;
}
.help-content p {
  margin: 4px 0;
}
.help-table {
  width: 100%;
  border-collapse: collapse;
  margin: 8px 0;
}
.help-table th, .help-table td {
  border: 1px solid #ebeef5;
  padding: 8px 12px;
  text-align: left;
  font-size: 13px;
}
.help-table th {
  background: #f5f7fa;
  font-weight: 600;
  color: #606266;
}
.help-limits {
  padding-left: 20px;
  margin: 8px 0;
}
.help-limits li {
  margin-bottom: 6px;
  font-size: 13px;
  line-height: 1.6;
}
</style>
