package com.moe.moetranslator.madoka

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.net.Uri
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.moe.moetranslator.R
import com.moe.moetranslator.databinding.FragmentMadokaBinding
import kotlinx.coroutines.launch
import live2dsdk.madoka.GLRenderer
import live2dsdk.madoka.LAppDelegate
import live2dsdk.madoka.LAppLive2DManager

class FunWithMadoka : Fragment() {
    private lateinit var binding: FragmentMadokaBinding
    private lateinit var viewModel: Live2DViewModel

    private lateinit var modelAdapter: Live2DModelAdapter
    private lateinit var expressionAdapter: Live2DExpressionAdapter
    private lateinit var motionAdapter: Live2DMotionAdapter

    private val pickFolderLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { handleFolderSelection(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMadokaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 显示进度对话框
        val progressDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Loading")
            .setMessage("Loading")
            .setCancelable(false)
            .create()

        progressDialog.show()

        setupGLSurfaceView()
        setupViewModel()
        setupAdapters()
        setupDrawers()
        setupClickListeners()
        observeData()

        progressDialog.dismiss()
    }

    override fun onStart() {
        super.onStart()
        LAppDelegate.getInstance().onStart(requireActivity())
    }

    override fun onResume() {
        super.onResume()
        binding.live2dContainer.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.live2dContainer.onPause()
        LAppDelegate.getInstance().onPause()
    }

    override fun onStop() {
        super.onStop()
        LAppDelegate.getInstance().onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        LAppDelegate.getInstance().onDestroy()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGLSurfaceView(){
        val glSurfaceView = binding.live2dContainer
        glSurfaceView.setEGLContextClientVersion(2) // OpenGL ES 2.0を利用

        val glRenderer = GLRenderer()
        glSurfaceView.setRenderer(glRenderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        glSurfaceView.setOnTouchListener { view, event ->
                val pointX = event.x
                val pointY = event.y
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        LAppDelegate.getInstance().onTouchBegan(pointX, pointY)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        LAppDelegate.getInstance().onTouchEnd(pointX, pointY)
//                        view.performClick()
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        LAppDelegate.getInstance().onTouchMoved(pointX, pointY)
                        true
                    }
                    else -> {
                        false
                    }
                }
        }
    }

    private fun setupViewModel() {
        val database = ModelInfoRoomDatabase.getDatabase(requireContext())
        val repository = ModelInfoRepository(database.ModelInfoDAO())
        val fileUtil = Live2DFileUtil(requireContext())

        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return Live2DViewModel(repository, fileUtil) as T
            }
        })[Live2DViewModel::class.java]
    }

    private fun setupAdapters() {
        modelAdapter = Live2DModelAdapter(
            onModelClick = { modelId ->
                val modelNumber = modelId.replace("model_", "").toInt()
                changeModel(modelNumber)
                viewModel.setCurrentModel(modelId)
                binding.drawerLayout.closeDrawers()
            },
            onModelLongClick = { model ->
                showRenameDialog(model.modelId, model.displayName) { newName ->
                    lifecycleScope.launch {
                        viewModel.updateModelName(model.modelId, newName)
                    }
                }
            }
        )

        expressionAdapter = Live2DExpressionAdapter(
            onExpressionClick = { fileName ->
                // 调用Live2D SDK的displayExpression
                binding.drawerLayout.closeDrawers()
            },
            onExpressionLongClick = { expression ->
                showRenameDialog(expression.id.toString(), expression.displayName) { newName ->
                    lifecycleScope.launch {
                        viewModel.updateExpressionName(expression.id, newName)
                    }
                }
            }
        )

        motionAdapter = Live2DMotionAdapter(
            onMotionClick = { fileName ->
                // 调用Live2D SDK的displayMotion
                binding.drawerLayout.closeDrawers()
            },
            onMotionLongClick = { motion ->
                showRenameDialog(motion.id.toString(), motion.displayName) { newName ->
                    lifecycleScope.launch {
                        viewModel.updateMotionName(motion.id, newName)
                    }
                }
            }
        )
    }

    private fun setupDrawers() {
        // 为抽屉设置自定义布局
        binding.navModels.inflateHeaderView(R.layout.item_drawer_content).apply {
            findViewById<RecyclerView>(R.id.recycler_view)?.apply {
                adapter = modelAdapter
                layoutManager = LinearLayoutManager(context)
            }
        }

        binding.navExpressionsAndMotions.inflateHeaderView(R.layout.item_drawer_content).apply {
            findViewById<RecyclerView>(R.id.recycler_view)?.apply {
                adapter = expressionAdapter
                layoutManager = LinearLayoutManager(context)
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnModelList.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.END)
            }

            btnImportModel.setOnClickListener {
                showImportModelDialog()
            }

            // 修改表情按钮的点击处理
            btnExpressions.setOnClickListener {
                // 打开左侧抽屉
                drawerLayout.openDrawer(GravityCompat.START)
                // 切换到表情列表
                navExpressionsAndMotions.getHeaderView(0)?.findViewById<RecyclerView>(R.id.recycler_view)?.apply {
                    adapter = expressionAdapter
                }
            }

            // 修改动作按钮的点击处理
            btnMotions.setOnClickListener {
                // 打开左侧抽屉
                drawerLayout.openDrawer(GravityCompat.START)
                // 切换到动作列表
                navExpressionsAndMotions.getHeaderView(0)?.findViewById<RecyclerView>(R.id.recycler_view)?.apply {
                    adapter = motionAdapter
                }
            }
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.allModels.collect { models ->
                        modelAdapter.submitList(models)
                    }
                }

                launch {
                    viewModel.currentExpressions.collect { expressions ->
                        expressionAdapter.submitList(expressions)
                    }
                }

                launch {
                    viewModel.currentMotions.collect { motions ->
                        motionAdapter.submitList(motions)
                    }
                }
            }
        }
    }

    private fun showRenameDialog(id: String, currentName: String, onConfirm: (String) -> Unit) {
        val context = context ?: return
        val editText = EditText(context).apply {
            setText(currentName)
            setSingleLine()
        }

        AlertDialog.Builder(context)
//            .setTitle(R.string.rename_dialog_title)
            .setTitle("R.string.rename_dialog_title")
            .setView(editText)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    onConfirm(newName)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showImportModelDialog() {
        val context = context ?: return
        val editText = EditText(context).apply {
            setSingleLine()
//            hint = getString(R.string.model_name_hint)
            hint = "getString(R.string.model_name_hint)"
        }

        MaterialAlertDialogBuilder(context)
            .setTitle("R.string.import_model_dialog_title")
            .setMessage("R.string.import_model_folder_message")  // 添加说明文字
            .setView(editText)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val modelName = editText.text.toString().trim()
                if (modelName.isNotEmpty()) {
                    pendingModelName = modelName
                    pickFolderLauncher.launch(null)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private var pendingModelName: String? = null

    private fun handleFolderSelection(uri: Uri) {
        val modelName = pendingModelName ?: return
        pendingModelName = null

        // 显示进度对话框
        val progressDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("R.string.importing_model_title")
            .setMessage("R.string.importing_model_message")
            .setCancelable(false)
            .create()

        progressDialog.show()

        lifecycleScope.launch {
            try {
                Log.d("FunWithMadoka", "uri:$uri \nImporting model with name: $modelName")
                val success = viewModel.importModel(uri, modelName)
                progressDialog.dismiss()

                val messageResId = if (success) {
                    "R.string.import_model_success"
                } else {
                    "R.string.import_model_failed"
                }
                Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                progressDialog.dismiss()
                Toast.makeText(context, "R.string.import_model_failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun changeModel(n: Int){
        LAppLive2DManager.getInstance().nextScene(n)
    }

    private fun displayExpression(){

    }

    private fun displayMotion(){

    }

    companion object {
        fun newInstance() = FunWithMadoka()
    }
}