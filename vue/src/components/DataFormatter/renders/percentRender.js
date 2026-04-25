export function percentRender(h, proxy) {
  const { data, $attrs, formatter } = proxy
  return (
    <span>{formatter.format(data, $attrs.total)}</span>
  )
}
