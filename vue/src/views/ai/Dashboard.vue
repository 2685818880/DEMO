<template>
  <div class="ai-dashboard">
    <h2 style="margin-bottom: 20px;">AI 智能分析平台</h2>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="8">
        <el-card shadow="hover" class="ai-card" @click.native="$router.push('/ai/inventory-prediction')">
          <div class="card-content">
            <div class="card-icon" style="background: #ecf5ff;">
              <i class="el-icon-data-line" style="font-size: 48px; color: #409eff;"></i>
            </div>
            <div class="card-info">
              <h3>库存预测</h3>
              <p>基于历史数据，智能预测未来库存需求趋势</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="ai-card" @click.native="$router.push('/ai/path-optimization')">
          <div class="card-content">
            <div class="card-icon" style="background: #fdf6ec;">
              <i class="el-icon-s-promotion" style="font-size: 48px; color: #e6a23c;"></i>
            </div>
            <div class="card-info">
              <h3>路径优化</h3>
              <p>优化仓库拣货路径，提升作业效率</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="ai-card" @click.native="$router.push('/ai/anomaly-detection')">
          <div class="card-content">
            <div class="card-icon" style="background: #fef0f0;">
              <i class="el-icon-warning-outline" style="font-size: 48px; color: #f56c6c;"></i>
            </div>
            <div class="card-info">
              <h3>异常检测</h3>
              <p>实时检测仓库运营异常，及时发现和预警</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="ai-card" @click.native="$router.push('/ai/chat-bi')">
          <div class="card-content">
            <div class="card-icon" style="background: #f0f9eb;">
              <i class="el-icon-chat-dot-square" style="font-size: 48px; color: #67c23a;"></i>
            </div>
            <div class="card-info">
              <h3>ChatBI 智能查询</h3>
              <p>自然语言查询库存数据，智能生成处理建议</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="ai-card" @click.native="$router.push('/ai/scheduling')">
          <div class="card-content">
            <div class="card-icon" style="background: #e8f4f8;">
              <i class="el-icon-date" style="font-size: 48px; color: #5dade2;"></i>
            </div>
            <div class="card-info">
              <h3>库存排班</h3>
              <p>智能排班管理，合理分配仓库人员班次</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="ai-card" @click.native="$router.push('/ai/allocation')">
          <div class="card-content">
            <div class="card-icon" style="background: #f5eef8;">
              <i class="el-icon-sort" style="font-size: 48px; color: #a569bd;"></i>
            </div>
            <div class="card-info">
              <h3>库存调拨</h3>
              <p>跨仓库货品调拨，优化库存分布结构</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="ai-card" @click.native="$router.push('/ai/providers')">
          <div class="card-content">
            <div class="card-icon" style="background: #f5f5f5;">
              <i class="el-icon-connection" style="font-size: 48px; color: #606266;"></i>
            </div>
            <div class="card-info">
              <h3>API 提供商</h3>
              <p>管理AI服务商密钥、查看模型列表</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <div slot="header">
        <span>AI 服务状态</span>
        <el-tag :type="agentStatus === 'online' ? 'success' : 'danger'" size="small" style="float: right;" effect="dark">
          <i :class="['el-icon-loading', { 'hide-loading': !loading }]" style="margin-right: 4px;"></i>
          {{ agentStatus === 'online' ? '在线' : '离线' }}
        </el-tag>
      </div>
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="status-item">
            <p class="status-label">Agent 总数</p>
            <p class="status-value">{{ agentCount }}</p>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-item">
            <p class="status-label">在线 Agent</p>
            <p class="status-value" style="color: #67c23a;">{{ onlineCount }}</p>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-item">
            <p class="status-label">离线 Agent</p>
            <p class="status-value" style="color: #f56c6c;">{{ agentCount - onlineCount }}</p>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-item">
            <p class="status-label">最后更新</p>
            <p class="status-value" style="font-size: 14px;">{{ lastUpdated || '--' }}</p>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card style="margin-top: 20px;">
      <div slot="header">
        <span>子 Agent 运行状态</span>
        <el-tag size="small" type="info" style="float: right;">
          每 30 秒自动刷新
        </el-tag>
      </div>
      <el-table :data="agents" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="Agent 名称" min-width="200" />
        <el-table-column label="类型" width="160">
          <template slot-scope="scope">{{ getTypeLabel(scope.row.type) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="scope.row.running ? 'success' : 'danger'" size="small">
              {{ scope.row.running ? '运行中' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import {getAgentStatus} from '@/api/ai'

export default {
  name: 'AiDashboard',
  data() {
    return {
      agentStatus: 'offline',
      agents: [],
      agentCount: 0,
      onlineCount: 0,
      loading: false,
      timer: null,
      lastUpdated: null
    }
  },
  mounted() {
    this.fetchStatus()
    this.timer = setInterval(() => {
      this.fetchStatus()
    }, 30000)
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
      this.timer = null
    }
  },
  methods: {
    async fetchStatus() {
      this.loading = true
      try {
        const res = await getAgentStatus()
        if (res.success && res.data) {
          this.agentStatus = res.data.status || 'offline'
          this.agents = res.data.agents || []
          this.agentCount = this.agents.length
          this.onlineCount = res.data.onlineAgents || 0
          this.lastUpdated = new Date().toLocaleTimeString()
        }
      } catch (e) {
        console.error('获取AI状态失败', e)
        this.agentStatus = 'offline'
        this.agents = []
        this.agentCount = 0
        this.onlineCount = 0
      } finally {
        this.loading = false
      }
    },
    getTypeLabel(type) {
      const map = {
        'WMS_INVENTORY_PREDICTION': '库存预测',
        'WMS_PATH_OPTIMIZATION': '路径优化',
        'WMS_ANOMALY_DETECTION': '异常检测',
        'WMS_SCHEDULING': '库存排班',
        'WMS_ALLOCATION': '库存调拨'
      }
      return map[type] || type
    }
  }
}
</script>

<style scoped>
.ai-dashboard {
  padding: 20px;
}
.ai-card {
  cursor: pointer;
  transition: transform 0.2s;
}
.ai-card:hover {
  transform: translateY(-4px);
}
.card-content {
  display: flex;
  align-items: center;
}
.card-icon {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.card-info {
  margin-left: 16px;
}
.card-info h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
}
.card-info p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}
.status-item {
  text-align: center;
  padding: 10px;
}
.status-label {
  color: #909399;
  font-size: 13px;
  margin: 0 0 8px 0;
}
.status-value {
  font-size: 20px;
  font-weight: bold;
  margin: 0;
}
.hide-loading {
  display: none;
}
</style>
