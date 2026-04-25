export function cutoffRender(h, proxy) {
  const { $attrs, $listeners, formatter } = proxy
  return (
    <el-tooltip
      {...{attrs: $attrs}}
      {...{on: $listeners}}
    >
      <div
        style={{
          width: $attrs.width,
          overflow: 'hidden',
          'white-space': 'nowrap',
          'text-overflow': 'ellipsis'
        }}
      >
        {formatter.data}
      </div>
    </el-tooltip>
  )
}
