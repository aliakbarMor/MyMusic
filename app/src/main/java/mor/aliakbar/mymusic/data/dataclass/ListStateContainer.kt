package mor.aliakbar.mymusic.data.dataclass

object ListStateContainer {

    var state: ListStateType = ListStateType.DEFAULT
        private set

    fun update(state: ListStateType) {
        this.state = state
    }

}
