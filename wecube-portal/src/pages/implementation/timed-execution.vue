<template>
  <div class=" ">
    <div class="report-container">
      <div class="item">
        {{ $t('datetime_range') }}:
        <DatePicker type="datetimerange" format="yyyy-MM-dd HH:mm:ss" @on-change="getDate"></DatePicker>
      </div>
      <div class="item">
        {{ $t('set_up_person') }}:
        <Input v-model="searchConfig.params.owner" style="width: 80%"></Input>
      </div>
      <div class="item">
        {{ $t('timing_type') }}:
        <Select v-model="searchConfig.params.scheduleMode" clearable style="width: 70%">
          <Option v-for="item in searchConfig.timingTypeOptions" :value="item.value" :key="item.value">{{
            item.label
          }}</Option>
        </Select>
      </div>
      <div class="item">
        <Button type="primary" @click="getUserScheduledTasks"> {{ $t('query') }}</Button>
        <Button @click="setTimedExecution"> {{ $t('full_word_add') }}</Button>
        <Button @click="exportData"> {{ $t('export') }}</Button>
      </div>
    </div>
    <Table size="small" ref="table" :columns="tableColumns" :max-height="MODALHEIGHT" :data="tableData"></Table>
    <Modal v-model="showModal" :fullscreen="fullscreen" width="1000" footer-hide>
      <p slot="header">
        <span>{{ $t('details') }}</span>
        <Icon
          v-if="!fullscreen"
          @click="fullscreen = true"
          style="float: right;margin: 3px 40px 0 0 !important;"
          type="ios-expand"
        />
        <Icon
          v-else
          @click="fullscreen = false"
          style="float: right;margin: 3px 40px 0 0 !important;"
          type="ios-contract"
        />
      </p>
      <Table :columns="detailTableColums" size="small" :max-height="MODALHEIGHT" :data="detailTableData"></Table>
    </Modal>
    <Modal v-model="timeConfig.isShow" :title="$t('timed_execution')">
      <Form :label-width="100" label-colon>
        <FormItem :label="$t('flow_name')">
          <Select v-model="timeConfig.params.selectedFlowInstance" filterable style="width:370px">
            <Option
              v-for="item in timeConfig.allFlowInstances"
              :value="item.id"
              :key="item.id"
              :label="
                item.procInstName +
                  ' ' +
                  item.entityDisplayName +
                  ' ' +
                  (item.createdTime || '0000-00-00 00:00:00') +
                  ' ' +
                  (item.operator || 'operator')
              "
            >
              <span>
                <span style="color:#2b85e4">{{ item.procInstName + ' ' }}</span>
                <span style="color:#515a6e">{{ item.entityDisplayName + ' ' }}</span>
                <span style="color:#ccc;padding-left:8px;float:right">{{ item.status }}</span>
                <span style="color:#ccc;float:right">{{ (item.createdTime || '0000-00-00 00:00:00') + ' ' }}</span>
                <span style="float:right;color:#515a6e;margin-right:20px">{{ item.operator || 'operator' }}</span>
              </span>
            </Option>
          </Select>
        </FormItem>
        <FormItem :label="$t('timing_type')">
          <Select
            v-model="timeConfig.params.scheduleMode"
            @on-change="timeConfig.params.time = '00:00:00'"
            style="width:370px"
          >
            <Option v-for="item in timeConfig.scheduleModeOptions" :key="item.value" :value="item.value">{{
              item.label
            }}</Option>
          </Select>
        </FormItem>
        <FormItem
          v-if="['Monthly', 'Weekly'].includes(timeConfig.params.scheduleMode)"
          :label="timeConfig.params.scheduleMode === 'Monthly' ? $t('day') : $t('week')"
        >
          <Select v-model="timeConfig.params.cycle" style="width:370px">
            <Option
              v-for="item in timeConfig.modeToValue[timeConfig.params.scheduleMode]"
              :key="item.value"
              :value="item.value"
              >{{ item.label }}</Option
            >
          </Select>
        </FormItem>
        <FormItem :label="$t('execute_date')">
          <TimePicker
            :value="timeConfig.params.time"
            @on-change="changeTimePicker"
            style="width:370px"
            :disabled-hours="
              timeConfig.params.scheduleMode === 'Hourly'
                ? [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23]
                : []
            "
            :clearable="false"
            format="HH:mm:ss"
          ></TimePicker>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="text" @click="timeConfig.isShow = false">{{ $t('bc_cancel') }}</Button>
        <Button type="primary" @click="saveTime">{{ $t('save') }}</Button>
      </div>
    </Modal>
  </div>
</template>

<script>
import {
  getUserScheduledTasks,
  deleteUserScheduledTasks,
  resumeUserScheduledTasks,
  getScheduledTasksByStatus,
  getProcessInstances,
  setUserScheduledTasks,
  stopUserScheduledTasks
} from '@/api/server'
export default {
  name: '',
  data () {
    return {
      showModal: false,
      fullscreen: false,
      detailTableColums: [
        {
          type: 'index',
          width: 60,
          align: 'center'
        },
        {
          title: this.$t('flow_name'),
          key: 'procDefName'
        },
        {
          title: this.$t('execute_date'),
          key: 'execTime'
        },
        {
          title: this.$t('status'),
          key: 'status'
        },
        {
          title: this.$t('table_action'),
          key: 'action',
          width: 100,
          align: 'center',
          render: (h, params) => {
            return (
              <div>
                <Button
                  onClick={() => this.jumpToHistory(params.row)}
                  type="primary"
                  size="small"
                  style="margin-right: 5px"
                >
                  {this.$t('bc_history_record')}
                </Button>
              </div>
            )
          }
        }
      ],
      detailTableData: [],

      MODALHEIGHT: 0,
      searchConfig: {
        params: {
          startTime: '',
          endTime: '',
          owner: '',
          scheduleMode: ''
        },
        timingTypeOptions: [
          { label: this.$t('Hourly'), value: 'Hourly' },
          { label: this.$t('Daily'), value: 'Daily' },
          { label: this.$t('Weekly'), value: 'Weekly' },
          { label: this.$t('Monthly'), value: 'Monthly' }
        ]
      },
      tableData: [],
      tableColumns: [
        {
          type: 'index',
          width: 60,
          align: 'center'
        },
        {
          title: this.$t('flow_name'),
          key: 'procDefName'
        },
        {
          title: this.$t('target_object'),
          key: 'entityDataName'
        },
        {
          title: this.$t('table_created_date'),
          key: 'createdTime'
        },
        {
          title: this.$t('set_up_person'),
          key: 'owner'
        },
        {
          title: this.$t('timing_type'),
          key: 'scheduleMode'
        },
        {
          title: this.$t('schedule_expr'),
          key: 'scheduleExpr'
        },
        {
          title: this.$t('status'),
          key: 'status'
        },
        {
          title: this.$t('failure_count'),
          key: 'totalFaultedInstances',
          width: 100,
          render: (h, params) => {
            return (
              <div>
                <span style="color:red">{params.row.totalFaultedInstances}</span>
                {params.row.totalFaultedInstances > 0 && (
                  <Button
                    style="margin-left:8px"
                    size="small"
                    type="primary"
                    ghost
                    onClick={() => this.getDetails(params.row, 'F')}
                    icon="ios-search"
                  ></Button>
                )}
              </div>
            )
          }
        },
        {
          title: this.$t('success_count'),
          key: 'totalCompletedInstances',
          width: 100,
          render: (h, params) => {
            return (
              <div>
                <span style="color:#2d8cf0">{params.row.totalCompletedInstances}</span>
                {params.row.totalCompletedInstances > 0 && (
                  <Button
                    style="margin-left:8px"
                    size="small"
                    type="primary"
                    ghost
                    onClick={() => this.getDetails(params.row, 'S')}
                    icon="ios-search"
                  ></Button>
                )}
              </div>
            )
          }
        },
        {
          title: this.$t('table_action'),
          key: 'action',
          width: 250,
          align: 'center',
          render: (h, params) => {
            return (
              <div>
                {params.row.status === 'Ready' && (
                  <Button onClick={() => this.pause(params.row)} type="warning" size="small" style="margin-right: 5px">
                    {this.$t('pause')}
                  </Button>
                )}
                {params.row.status === 'Stopped' && (
                  <Button onClick={() => this.resume(params.row)} type="info" size="small" style="margin-right: 5px">
                    {this.$t('start_up')}
                  </Button>
                )}
                <Button onClick={() => this.remove(params.row)} type="error" size="small" style="margin-right: 5px">
                  {this.$t('delete')}
                </Button>
                <Button
                  onClick={() => this.getDetails(params.row, '')}
                  type="primary"
                  size="small"
                  style="margin-right: 5px"
                >
                  {this.$t('list')}
                </Button>
              </div>
            )
          }
        }
      ],
      timeConfig: {
        isShow: false,
        params: {
          selectedFlowInstance: '',
          scheduleMode: 'Monthly',
          time: '00:00:00',
          cycle: ''
        },
        scheduleModeOptions: [
          { label: this.$t('Hourly'), value: 'Hourly' },
          { label: this.$t('Daily'), value: 'Daily' },
          { label: this.$t('Weekly'), value: 'Weekly' },
          { label: this.$t('Monthly'), value: 'Monthly' }
        ],
        modeToValue: {
          Monthly: [
            { label: '1', value: 1 },
            { label: '2', value: 2 },
            { label: '3', value: 3 },
            { label: '4', value: 4 },
            { label: '5', value: 5 },
            { label: '6', value: 6 },
            { label: '7', value: 7 },
            { label: '8', value: 8 },
            { label: '9', value: 9 },
            { label: '10', value: 10 },
            { label: '11', value: 11 },
            { label: '12', value: 12 },
            { label: '13', value: 13 },
            { label: '14', value: 14 },
            { label: '15', value: 15 },
            { label: '16', value: 16 },
            { label: '17', value: 17 },
            { label: '18', value: 18 },
            { label: '19', value: 19 },
            { label: '20', value: 20 },
            { label: '21', value: 21 },
            { label: '22', value: 22 },
            { label: '23', value: 23 },
            { label: '24', value: 24 },
            { label: '25', value: 25 },
            { label: '26', value: 26 },
            { label: '27', value: 27 },
            { label: '28', value: 28 },
            { label: '29', value: 29 },
            { label: '30', value: 30 },
            { label: '31', value: 31 }
          ],
          Weekly: [
            { label: this.$t('Mon'), value: 1 },
            { label: this.$t('Tue'), value: 2 },
            { label: this.$t('Wed'), value: 3 },
            { label: this.$t('Thu'), value: 4 },
            { label: this.$t('Fri'), value: 5 },
            { label: this.$t('Sat'), value: 6 },
            { label: this.$t('Sun'), value: 7 }
          ]
        },
        allFlowInstances: []
      }
    }
  },
  mounted () {
    this.MODALHEIGHT = document.body.scrollHeight - 300
    this.getUserScheduledTasks()
  },
  methods: {
    async getProcessInstances () {
      let { status, data } = await getProcessInstances()
      if (status === 'OK') {
        this.timeConfig.allFlowInstances = data.filter(item => item.status === 'Completed')
      }
    },
    changeTimePicker (time) {
      this.timeConfig.params.time = time
    },
    async saveTime () {
      const found = this.timeConfig.allFlowInstances.find(_ => _.id === this.timeConfig.params.selectedFlowInstance)
      if (!found) return
      let scheduleExpr = ''
      if (['Hourly', 'Daily'].includes(this.timeConfig.params.scheduleMode)) {
        scheduleExpr = this.timeConfig.params.time
        if (this.timeConfig.params.scheduleMode === 'Hourly') {
          scheduleExpr = this.timeConfig.params.time.substring(3)
        }
      } else {
        scheduleExpr = this.timeConfig.params.cycle + ' ' + this.timeConfig.params.time
      }
      let params = {
        scheduleMode: this.timeConfig.params.scheduleMode,
        scheduleExpr: scheduleExpr,
        procDefName: found.procInstName,
        procDefId: found.procDefId,
        entityDataName: found.entityDisplayName,
        entityDataId: found.entityDataId
      }
      const { status } = await setUserScheduledTasks(params)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: 'Success'
        })
        this.timeConfig.isShow = false
        this.getUserScheduledTasks()
      }
    },
    async setTimedExecution () {
      this.getProcessInstances()
      this.timeConfig.params.selectedFlowInstance = ''
      this.timeConfig.params.scheduleMode = 'Monthly'
      this.timeConfig.params.time = '00:00:00'
      this.timeConfig.params.cycle = ''
      this.timeConfig.isShow = true
    },
    exportData () {
      this.$refs.table.exportCsv({
        filename: 'timed_execution'
      })
    },
    jumpToHistory (row) {
      this.$emit('jumpToHistory', row.procInstId)
    },
    async getDetails (row, rowStatus) {
      const params = {
        userTaskId: row.id,
        procInstanceStatus: rowStatus
      }
      const { status, data } = await getScheduledTasksByStatus(params)
      if (status === 'OK') {
        this.showModal = true
        this.detailTableData = data
      }
    },
    async resume (row) {
      const params = [
        {
          id: row.id
        }
      ]
      const { status, message } = await resumeUserScheduledTasks(params)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
        this.getUserScheduledTasks()
      }
    },
    async pause (row) {
      const params = [
        {
          id: row.id
        }
      ]
      const { status, message } = await stopUserScheduledTasks(params)
      if (status === 'OK') {
        this.$Notice.success({
          title: 'Success',
          desc: message
        })
        this.getUserScheduledTasks()
      }
    },
    remove (row) {
      this.$Modal.confirm({
        title: this.$t('confirm_to_delete'),
        'z-index': 1000000,
        onOk: async () => {
          const params = [
            {
              id: row.id
            }
          ]
          const { status, message } = await deleteUserScheduledTasks(params)
          if (status === 'OK') {
            this.$Notice.success({
              title: 'Delete Success',
              desc: message
            })
            this.getUserScheduledTasks()
          }
        },
        onCancel: () => {}
      })
    },
    async getUserScheduledTasks () {
      let params = JSON.parse(JSON.stringify(this.searchConfig.params))
      const keys = Object.keys(params)
      keys.forEach(key => {
        if (params[key] === '') {
          delete params[key]
        }
      })
      const { status, data } = await getUserScheduledTasks(params)
      if (status === 'OK') {
        this.tableData = data
      }
    },
    getDate (dateRange) {
      this.searchConfig.params.startTime = dateRange[0]
      this.searchConfig.params.endTime = dateRange[1]
    }
  },
  components: {}
}
</script>

<style scoped lang="scss">
.report-container {
  display: flex;
  flex-wrap: wrap;
  // margin-bottom: 16px;
}
.ivu-form-item {
  margin-bottom: 8px;
}
.item {
  width: 21%;
  margin: 8px;
}
</style>
