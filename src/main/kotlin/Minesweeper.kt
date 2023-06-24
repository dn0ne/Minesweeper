import kotlin.random.Random

class Minesweeper(private val fieldSize: Int) {
    private val fieldBounds = 0 until fieldSize
    private val range = -1..1
    private val minesCount: Int
    init {
        print("How many mines do you want on the field?  ")
        minesCount = readln().toInt()
    }

    private var isWon = true

    private val grid: MutableList<MutableList<Cell>>
    init {
        grid = createGrid()
        countMinesAroundCells()
    }

    fun start() {
        printGrid()

        while (!isFinished()) {
            print("Set/unset mine marks (form: x y mine) or claim a cell as free (form: x y free): ")
            val (x, y, action) = readln().trimIndent().split(Regex("\\s+"))
            processAction(y.toInt() - 1, x.toInt() - 1, action)
            printGrid()
        }

        println(
            if (isWon) "Congratulations! You found all the mines!"
            else "You stepped on a mine and failed!"
        )
    }

    private fun createGrid(): MutableList<MutableList<Cell>> {
        val grid = MutableList(fieldSize) { MutableList(fieldSize) { Cell() } }

        var k = 0
        while(k < minesCount) {
            val (y, x) = List(2) { Random.nextInt(fieldSize) }
            if (!grid[y][x].isMine) {
                grid[y][x].isMine = true
                k++
            }
        }

        return grid
    }

    private fun countMinesAroundCells() {
        for (i in fieldBounds) {
            for (j in fieldBounds) {
                if (!grid[i][j].isMine) grid[i][j].minesAroundCount = countMinesAroundCell(i, j)
            }
        }
    }

    private fun countMinesAroundCell(y: Int, x: Int): Int {
        var minesAround = 0

        for (i in range) {
            for (j in range) {
                if (
                    (y + i) in fieldBounds &&
                    (x + j) in fieldBounds &&
                    grid[y + i][x + j].isMine
                ) {
                    minesAround++
                }
            }
        }

        return minesAround
    }

    private fun exploreCell(y: Int, x: Int) {
        if (!(y in fieldBounds && x in fieldBounds) || grid[y][x].isExplored) return

        grid[y][x].isExplored = true

        if (grid[y][x].isMine) {
            isWon = false
            exploreAllMines()
            return
        }

        if (grid[y][x].minesAroundCount > 0) return

        for (i in range) {
            for (j in range) {
                exploreCell(y + i, x + j)
            }
        }
    }

    private fun exploreAllMines() {
        grid.flatten().forEach { if (it.isMine) it.isExplored = true }
    }

    private fun processAction(y: Int, x: Int, action: String) {
        if (y in fieldBounds && x in fieldBounds) {
            when (action) {
                "mine" -> {
                    grid[y][x].isMarked = !grid[y][x].isMarked
                }
                "free" -> {
                    exploreCell(y, x)
                }
            }
        }
    }

    private fun isFinished(): Boolean {
        return !isWon || checkMineMarked() || checkAllExplored()
    }

    private fun checkMineMarked(): Boolean {
        grid.flatten().forEach { if (it.isMine != it.isMarked) return false }
        return true
    }
    private fun checkAllExplored(): Boolean {
        grid.flatten().forEach { if (!it.isMine != it.isExplored) return false }
        return true
    }

    private fun printGrid() {
        println(" |123456789|")
        println("—|—————————|")
        grid.forEachIndexed { index, cells ->
            print("${index + 1}|")
            print(cells.joinToString(""))
            println("|")
        }
        println("—|—————————|")
    }

    private class Cell {
        var isMarked = false
        var isMine = false
        var isExplored = false
        var minesAroundCount = 0

        override fun toString(): String {
            return if (isExplored) {
                if (isMine) "X" else if (minesAroundCount > 0) minesAroundCount.toString() else "/"
            } else if (isMarked) "*" else "."
        }
    }
}