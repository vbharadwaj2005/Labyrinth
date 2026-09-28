package com.labyrinth.game.logic

import com.labyrinth.game.model.Direction

data class MazeRect(val x: Float, val y: Float, val w: Float, val h: Float)

class MazeEngine(private val rng: kotlin.random.Random = kotlin.random.Random.Default) {

    var cellWidth = 0
        private set
    var cellHeight = 0
        private set
    var maxX = 0
        private set
    var maxY = 0
        private set
    var startCellIndex = 0
        private set

    var currentPosition = -1
        private set

    private var cells = intArrayOf()
    private val frontier = ArrayList<Edge>()
    private val carvedRects = ArrayList<MazeRect>()

    private var cellSize = 0f
    private var cellSpacing = 0f

    data class Edge(val index: Int, val direction: Int)

    fun resetForNextMaze(width: Int, height: Int) {
        cellWidth = width
        cellHeight = height
        maxX = width - 1
        maxY = height - 1

        cells = IntArray(width * height) { -1 }
        frontier.clear()
        carvedRects.clear()

        startCellIndex = maxY * width
        cells[startCellIndex] = 0
        frontier.add(Edge(startCellIndex, Direction.N))
        frontier.add(Edge(startCellIndex, Direction.E))

        generateMaze()
    }

    fun applyMetrics(cellSize: Float, cellSpacing: Float) {
        this.cellSize = cellSize
        this.cellSpacing = cellSpacing
    }

    private fun generateMaze() {
        carvePath(startCellIndex)
        while (!exploreFrontier()) {
        }
    }

    private fun exploreFrontier(): Boolean {
        if (frontier.isEmpty()) return true
        val edge = popRandom() ?: return true
        val i0 = edge.index
        val d0 = edge.direction
        val i1 = i0 + indexStep(d0)
        if (i1 < 0 || i1 >= cells.size || cells[i1] != -1) return false

        val d1 = Direction.opposite(d0)
        cells[i0] = cells[i0] or d0
        cells[i1] = d1
        carvePath(i1)

        when (d0) {
            Direction.N -> carveSouth(i1)
            Direction.S -> carveSouth(i0)
            Direction.W -> carveEast(i1)
            Direction.E -> carveEast(i0)
        }

        val x1 = i1 % cellWidth
        val y1 = i1 / cellWidth
        if (y1 > 0 && cells[i1 - cellWidth] == -1) frontier.add(Edge(i1, Direction.N))
        if (y1 < maxY && cells[i1 + cellWidth] == -1) frontier.add(Edge(i1, Direction.S))
        if (x1 > 0 && cells[i1 - 1] == -1) frontier.add(Edge(i1, Direction.W))
        if (x1 < maxX && cells[i1 + 1] == -1) frontier.add(Edge(i1, Direction.E))
        return false
    }

    fun hasOpening(index: Int, dir: Int): Boolean {
        if (index < 0 || index >= cells.size) return false
        val bit = cells[index]
        return bit != -1 && (bit and dir) != 0
    }

    fun move(direction: Int): Boolean {
        if (currentPosition < 0) return false
        if (!hasOpening(currentPosition, direction)) return false
        currentPosition += indexStep(direction)
        return true
    }

    fun setPosition(index: Int) {
        currentPosition = index
    }

    fun resetPosition() {
        currentPosition = startCellIndex
    }

    fun atGoal(): Boolean = currentPosition == maxX

    fun paths(): List<MazeRect> = carvedRects

    fun goalRect(): MazeRect = cellRect(maxX)

    fun playerCenter(index: Int): Pair<Float, Float> {
        val r = cellRect(index)
        return (r.x + cellSize / 2f) to (r.y + cellSize / 2f)
    }

    private fun indexStep(dir: Int): Int = Direction.indexChange(dir, cellWidth)

    private fun popRandom(): Edge? {
        if (frontier.isEmpty()) return null
        val i = rng.nextInt(frontier.size)
        val element = frontier[i]
        frontier[i] = frontier[frontier.size - 1]
        frontier.removeAt(frontier.size - 1)
        return element
    }

    private fun cellRect(index: Int): MazeRect {
        val col = index % cellWidth
        val row = index / cellWidth
        val x = col * (cellSize + cellSpacing) + cellSpacing
        val y = row * (cellSize + cellSpacing) + cellSpacing
        return MazeRect(x, y, cellSize, cellSize)
    }

    private fun carvePath(index: Int) {
        carvedRects.add(cellRect(index))
    }

    private fun carveEast(index: Int) {
        val r = cellRect(index)
        carvedRects.add(MazeRect(r.x + cellSize, r.y, cellSpacing, cellSize))
    }

    private fun carveSouth(index: Int) {
        val r = cellRect(index)
        carvedRects.add(MazeRect(r.x, r.y + cellSize, cellSize, cellSpacing))
    }
}